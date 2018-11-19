# Cover Type Prediction of Forests - Kaggle competition

**Use cartographic variables to classify forest categories with machine learning.** 

Details about the Kaggle competition at the following URL: <br>https://www.kaggle.com/c/sd701-cover-type-prediction-of-forests/

This report contains the analysis about the methodology used to predict the forest cover type using the dataset provided in the Kaggle competition which corresponds to cartographic variables collected from US Geological Survey and US Forest Service (USFS).

In this project I started with the PYSPARK.ML package, however I finally used the Python Scikit-learn tools for data mining and data analysis. I continued with the Scikit-learn library mainly for two reasons:

- for the same prediction algorithm (ex: RandomForestClassifier) with the same parameters, better results are obtained by Scikit-learn compared to PYSPARK.ML package.
- Scikit-learn contains ExtraTreesClassifier algorithm which is not available in PYSPARK.ML. ExtraTreesClassifier performs better than RandomForestClassifier for the current dataset. More generaly, Extra-Trees seems to get higher performance in presence of noisy features. A very good article on Extra-(Randomized)-Trees (ET) is provided by Pierre Geurts, Damien Ernst, Louis Wehenke at this link: ["Extremely randomized trees"](https://orbi.uliege.be/bitstream/2268/9357/1/geurts-mlj-advance.pdf).

In the following rows I have described the data analysis and all the actions I have done in order to find the best predictive model which better predicts the cover type. Each action or feature change was tested individually to see if it makes a positive impact on the predictions or not. When testing different prediction models I have used cross validation to determine the optimal values for the correspondent input parameters. More precisely, I had split the training dataset in two parts, the first 80% used to train the model and the rest of 20% to validate the predictions and calculate the accuracy. The final predictions were done on the test dataset and the best accuracy (0.95988) obtained on the Kaggle competition was achieved with the ExtraTreesClassifier from Scikit-learn.
