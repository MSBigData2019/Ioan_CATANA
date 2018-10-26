import requests
import re
import json
import pandas as pd


website_url = "https://open-medicaments.fr/api/v1/medicaments?query=paracetamol&limit=100"

res = requests.get(website_url)
response_object = json.loads(res.text)

df = pd.DataFrame(response_object)

reg = r"([\D]*)(\d+)(.*),([\w\s]*)"
string = "PARACETAMOL ZYDUS 500 mg, gélule"
match = re.search(reg, string)

df = df["denomination"].str.extract(reg)
df["nom"] = df[0].str.split(" ").str[0]
df["societe"] = df[0].str.split(" ").str[1]
df["factor"] = 1000
df["factor"] = df["factor"].where(df[2].str.strip() == "g", 1)
df["dosage"] = df[1].fillna(0).astype(int) * df["factor"]
df['type'] = df[3]

df.drop(df.columns[[0, 1, 2, 3]], axis=1, inplace=True)

print(df.head(10))

