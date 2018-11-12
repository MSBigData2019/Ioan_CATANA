
# MS BGD: Spark TP 3 (2018-2019) <br>Machine learning avec Spark

## Objectif
Réaliser un modèle capable de prédire si une campagne Kickstarter va atteindre son objectif ou non, en utilisant les données disponibles sur kaggle.com, avec leur description:

https://www.kaggle.com/codename007/funding-successful-projects

## Projet
L’IDE IntelliJ a été utilisé pour le développement de ce projet.

Le code source se trouve dans : TP3_Spark/src/main/scala/com/sparkProject/

Les données ont été nettoyées préalablement et sont incluses dans le répertoire suivant:
TP3_Spark/data/TP3/prepared_trainingset<br>
D'ailleurs le fichier source Trainer.scala contient le chemin relatif (data/TP3/prepared_trainingset) vers les donées, donc il faut juste s'assurer que les données sont bien présentes à cet entroit, sinon il est aussi possible de changer l'emplacement en modifiant la variable dfsFilename.

Pour lancer le projet il faut se placer au niveau du répertoire **TP3_Spark** et puis exécuter la commande suivante:

./build_and_submit.sh Trainer

Ci-dessous, une visualisation du lancement de la commande précedente:

Number of rows in DataFrame: 107614<br>
Number of columns in DataFrame: 14<br>
4.i) Create the pipeline<br>
5.k) Splitter les donnees en Training Set et Test Set<br>
5.l) Parametres du modele pour la grid-search<br>
5.l) Lancer la validation croisee sur le dataset training<br>
5.m) Tester le modele obtenu sur les donnees test<br>                               
Le F1 score obtenu : 0.6552977860994622<br>
+------------+-----------+-----+<br>                                                
|final_status|predictions|count|<br>
+------------+-----------+-----+<br>
|           1|        0.0| 1021|<br>
|           0|        1.0| 2835|<br>
|           1|        1.0| 2445|<br>
|           0|        0.0| 4513|<br>
+------------+-----------+-----+<br>


