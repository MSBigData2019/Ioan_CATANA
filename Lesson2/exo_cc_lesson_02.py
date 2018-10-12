# coding: utf-8
import requests
from bs4 import BeautifulSoup

website_prefix = "https://www.darty.com/nav/recherche?s=prix_desc&npk=1&text="
website_sufix = "&fa=756"
marques_data = {"dell":[], "acer":[]}

def _handle_request_result_and_build_soup(request_result):
    if request_result.status_code == 200:
        html_doc =  request_result.text
        soup = BeautifulSoup(html_doc,"html.parser")
        return soup

def get_meilleurs_remises_for_page(page_url):
    res = requests.get(page_url)
    soup = _handle_request_result_and_build_soup(res)
    l1 = soup.find_all("p", class_="darty_prix_barre_remise darty_small separator_top")
    disc_l = list(map(lambda x: x.text, l1))
    return disc_l

#
# Main code

for marque in marques_data:
    disc_list = get_meilleurs_remises_for_page(website_prefix + marque + website_sufix)
    marques_data[marque] = disc_list
    print(marque, marques_data[marque])
