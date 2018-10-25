# coding: utf-8

import requests
import pandas as pd
import json

website_prefix = "https://maps.googleapis.com/maps/api/distancematrix/json?origins={}&destinations={}&key={}"

def get_apikey(file_name):
    file = open(file_name, "r")
    apikey = file.readline()
    file.close()
    return apikey

def get_distance_villes(liste_villes, apikey):
    url = website_prefix.format(liste_villes, liste_villes, apikey)
    res = requests.get(url)
    response_object = json.loads(res.text)
    distances = list(map(lambda x : list(map(lambda y : y['distance']['text'], x['elements'])), response_object['rows']))
    matrix_villes = pd.DataFrame(distances, columns = df_villes["Ville"], index = df_villes["Ville"])
    return matrix_villes

#
# Main code starts here
apikey = get_apikey("apikey.txt")

df_villes = pd.read_excel("population.xls", headers=None)[:50]
df_villes["Ville"] = df_villes["Ville"] + ", France"

liste_villes = "|".join(df_villes["Ville"])
dist_villes = get_distance_villes(liste_villes, apikey)
print(dist_villes)



