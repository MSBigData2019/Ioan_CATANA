package com.sparkProject

import org.apache.spark.SparkConf
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.{CountVectorizer, IDF, RegexTokenizer, StopWordsRemover, StringIndexer, OneHotEncoder, VectorAssembler}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, MulticlassClassificationEvaluator}
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}

object Trainer {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAll(Map(
      "spark.scheduler.mode" -> "FIFO",
      "spark.speculation" -> "false",
      "spark.reducer.maxSizeInFlight" -> "48m",
      "spark.serializer" -> "org.apache.spark.serializer.KryoSerializer",
      "spark.kryoserializer.buffer.max" -> "1g",
      "spark.shuffle.file.buffer" -> "32k",
      "spark.default.parallelism" -> "12",
      "spark.sql.shuffle.partitions" -> "12",
      "spark.driver.maxResultSize" -> "2g"
    ))

    val spark = SparkSession
      .builder
      .config(conf)
      .appName("TP_spark")
      .getOrCreate()

    /*******************************************************************************
      *
      *       TP 3
      *
      *       - lire le fichier sauvegarder précédemment
      *       - construire les Stages du pipeline, puis les assembler
      *       - trouver les meilleurs hyperparamètres pour l'entraînement du pipeline avec une grid-search
      *       - Sauvegarder le pipeline entraîné
      *
      *       if problems with unimported modules => sbt plugins update
      *
      ********************************************************************************/

    /*
     * CHARGER le DATAFRAME
     */

    // 1. Charger le dataframe depuis le fichier parquet
    val dfsFilename = "data/TP3/prepared_trainingset"
    val clean_df: DataFrame = spark
      .read
      .parquet(dfsFilename)

    // Afficher le nombre de lignes et le nombre de colonnes dans le dataFrame
    val nb_rows = clean_df.count()
    println("Number of rows in DataFrame: " + nb_rows)
    val nb_cols = clean_df.columns.size
    println("Number of columns in DataFrame: " + nb_cols)

    /*
     * TF-IDF
     */

    // 2.a) Separer les textes en mots
    val tokenizer = new RegexTokenizer()
      .setPattern("\\W+")
      .setGaps(true)
      .setInputCol("text")
      .setOutputCol("tokens")

    // 2.b) Retirer les stop words
    val remover = new StopWordsRemover()
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol("filtered")

    // 2.c) Partie TF
    val tf_cv = new CountVectorizer()
      .setInputCol(remover.getOutputCol)
      .setOutputCol("tf")

    // 2.d) Partie IDF
    val idf = new IDF()
      .setInputCol(tf_cv.getOutputCol)
      .setOutputCol("tfidf")

    // 3.e) Convertir la variable country2 en quantites numeriques
    val indexer_country = new StringIndexer()
      .setInputCol("country2")
      .setOutputCol("country_indexed")
      .setHandleInvalid("skip")

    // 3.f) Convertir la variable currency2 en quantites numeriques
    val indexer_currency = new StringIndexer()
      .setInputCol("currency2")
      .setOutputCol("currency_indexed")
      .setHandleInvalid("skip")

    // 3.g) Transformer les 2 categories precedentes avec OneHotEncoder
    val encoder_country = new OneHotEncoder()
      .setInputCol("country_indexed")
      .setOutputCol("country_onehot")

    val encoder_currency = new OneHotEncoder()
      .setInputCol("currency_indexed")
      .setOutputCol("currency_onehot")

    /*
     * VECTOR ASSEMBLER
     */

    // 4. Mettre les donnees sous une forme utilisable par Spark.ML
    // 4.h) Assembler les features dans une seule colonne “features”.
    val assembler = new VectorAssembler()
      .setInputCols(Array("tfidf", "days_campaign", "hours_prepa", "goal",
        "country_onehot", "currency_onehot"))
      .setOutputCol("features")

    /*
     * MODEL (LOGISTIC REGRESSION)
     */

    // 4.i) Choix de la regression logistique en tant que modele de classification
    val lr = new LogisticRegression()
      .setElasticNetParam(0.0)
      .setFitIntercept(true)
      .setFeaturesCol("features")
      .setLabelCol("final_status")
      .setStandardization(true)
      .setPredictionCol("predictions")
      .setRawPredictionCol("raw_predictions")
      .setThresholds(Array(0.7, 0.3))
      .setTol(1.0e-6)
      .setMaxIter(300)

    /*
     * PIPELINE
     */

    // 4.i) Create the pipeline
    println("4.i) Create the pipeline")
    val pipeline = new Pipeline()
      .setStages(Array(tokenizer, remover,
        tf_cv, idf, indexer_country, encoder_country,
        indexer_currency, encoder_currency,
        assembler, lr))

    /*
     * SPLIT : TRAINING & TEST
     */

    // 5. Entrainement et tuning du modele
    // 5.k) Splitter les donnees en Training Set et Test Set
    println("5.k) Splitter les donnees en Training Set et Test Set")
    val Array(training, test) = clean_df.randomSplit(Array(0.9, 0.1), seed = 99999)

    /*
     * PARAMS GRID-SEARCH
     */

    // 5.l) Parametres de grid-search pour la validation croisee :
    // Creation d'une grid avec le valeurs de 10e-8 à 10e-2 par pas de 2.0 en echelle logarithmique
    // Pour minDF de countVectorizer on veut tester les valeurs de 55 a 95 par pas de 20
    println("5.l) Parametres du modele pour la grid-search")
    val paramGrid = new ParamGridBuilder()
      .addGrid(lr.regParam, Array(10e-8, 10e-6, 10e-4, 10e-2))
      .addGrid(tf_cv.minDF,(55.0 to 95.0 by 20.0))
      .build()

    val evaluator_f1 = new MulticlassClassificationEvaluator()
      .setLabelCol("final_status")
      .setPredictionCol("predictions")
      .setMetricName("f1")

    /*
     * CONFIGURE CROSS VALIDATION
     */

    // Utiliser 70% des donnees pour l’entrainement et 30% pour la validation
    val train_cv = new TrainValidationSplit()
      .setEstimator(pipeline)
      .setEvaluator(evaluator_f1)
      .setEstimatorParamMaps(paramGrid)
      .setTrainRatio(0.7)

    /*
     * TRAINING MODEL
     */

    // Lancer la validation croisee sur le dataset training
    println("5.l) Lancer la validation croisee sur le dataset training")
    val lr_cv_model = train_cv.fit(training)

    /*
     * TESTING MODEL
     */

    // 5.m) Tester le modele obtenu sur les donnees test
    println("5.m) Tester le modele obtenu sur les donnees test")
    val df_predictions = lr_cv_model
      .transform(test)
      .select("features", "final_status", "predictions", "raw_predictions")

    /*
     * EVALUATING MODEL
     */

    // Evaluer les predictions en utilisant le F1 score
    val f1_score = evaluator_f1.evaluate(df_predictions)

    // Afficher le F1 score
    println("Le F1 score obtenu : "+ f1_score)
    df_predictions.groupBy("final_status", "predictions")
      .count.show()

    // Evaluer la precision (accuracy)
    val evaluator_acc = new MulticlassClassificationEvaluator()
      .setLabelCol("final_status")
      .setPredictionCol("predictions")
      .setMetricName("accuracy")

    // obtention de la mesure de performance
    val accuracy = evaluator_acc.evaluate(df_predictions)
    println("Precision obtenue : " + accuracy)

    // Sauvegarder le modele entraine pour pouvoir le reutiliser plus tard
    lr_cv_model
      .write
      .overwrite()
      .save("data/TP3/LRModel")

  }
}
