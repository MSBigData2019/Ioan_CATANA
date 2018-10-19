# coding: utf-8

import requests
import unittest
import re
import pandas as pd
from bs4 import BeautifulSoup
import json
from requests.auth import HTTPBasicAuth

website_url = "https://gist.github.com/paulmillr/2657075"
users_dict = {}

def get_token(file_name):
    file = open(file_name, "r")
    token = file.readline()
    file.close()
    return token

def get_users_list(url):
    dfs = pd.read_html(url)
    users_names = dfs[0]["User"]
    users_list = []
    for user in users_names:
        users_list.append(user.split()[0])
    return users_list

def get_json_from_link(url, my_token):
    token_header = {'Authorization' : 'token ' + my_token}
    req = requests.get(url, headers=token_header)
    json_res = json.loads(req.text)
    return json_res

def get_avg_stars(json_link, my_token):
    star_count = 0
    json_response = get_json_from_link(json_link, my_token)
    if len(json_response) == 0:
        return 0
    else:
        for elem_dict in json_response:
            star_count = star_count + elem_dict['stargazers_count']
        return star_count / len(json_response)

#
# Main code starts here
my_token = get_token("token.txt")

users_list = get_users_list(website_url)

for user in users_list:
    user_link = "https://api.github.com/users/" + user + "/repos?per_page=100"
    users_dict[user] = get_avg_stars(user_link, my_token)

#print(users_dict)

sorted_list = sorted(zip(users_dict.values(), users_dict.keys()), reverse=True)
print(sorted_list)

