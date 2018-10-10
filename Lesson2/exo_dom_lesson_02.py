# coding: utf-8
import requests
import unittest
from bs4 import BeautifulSoup

website_prefix = "https://www.reuters.com/finance/stocks/financial-highlights/"
companies_data = {"Aribus":[], "LVMH":[], "Danone":[]}
companies_names = {"AIR.PA":"Aribus", "LVMH.PA":"LVMH", "DANO.PA":"Danone"}

def _handle_request_result_and_build_soup(request_result):
    if request_result.status_code == 200:
        html_doc =  request_result.text
        soup = BeautifulSoup(html_doc,"html.parser")
        return soup

def _convert_string_to_float(string):
    string = string.replace(',', '')
    if string[0] == '(':
        string = string[2:-2]
    else:
        string = string.replace('-', '0')
    return float(string)

def get_company_data_for_page(page_url):
    results_data = []
    results_quarter = []
    results_stock = []
    results_owned = []
    results_divYield = []
    res = requests.get(page_url)
    soup = _handle_request_result_and_build_soup(res)

    # get last quarter information
    page_tables = soup.find_all("table", class_= "dataTable")
    quarters_tr = page_tables[0].find_all("tr", class_="stripe")
    quartersData = quarters_tr[0].find_all("td", class_="data")[1:-1]
    results_quarter.append(_convert_string_to_float(quartersData[0].text.strip()))
    results_quarter.append(_convert_string_to_float(quartersData[1].text.strip()))
    results_quarter.append(_convert_string_to_float(quartersData[2].text.strip()))
    results_data.append(results_quarter)

    # get stock price value and change in percent
    price_class = "sectionQuote nasdaqChange"
    stockDiv = soup.find_all("div", class_=price_class)
    stockSpan = stockDiv[0].find_all("span")
    stockPrice = stockSpan[1].text.strip()
    results_stock.append(_convert_string_to_float(stockPrice))
    percent_class="valueContentPercent"
    stockPerSpan = soup.find_all("span", class_=percent_class)
    percentValue = stockPerSpan[0].text.strip()[2:-2]
    results_stock.append(_convert_string_to_float(percentValue))
    results_data.append(results_stock)

    # get owned institutions
    ownedInstitutions = page_tables[1].find_all("td", class_="data")[-3:]
    results_owned.append(_convert_string_to_float(ownedInstitutions[0].text.strip()))
    results_owned.append(_convert_string_to_float(ownedInstitutions[1].text.strip()))
    results_owned.append(_convert_string_to_float(ownedInstitutions[2].text.strip()))
    results_data.append(results_owned)

    # get dividend yield
    dividendYield = page_tables[2].find_all("td", class_="data")[0:3]
    results_divYield.append(_convert_string_to_float(dividendYield[0].text.strip()))
    results_divYield.append(_convert_string_to_float(dividendYield[1].text.strip()))
    results_divYield.append(_convert_string_to_float(dividendYield[2].text.strip()))
    results_data.append(results_divYield)

    return results_data

#
# Main code
print("The companies data is stored into a dictionary having the keys the company code on the Reuters web site")
print("Each row in the dictionary contains 4 lists of values, represented as following:")
print("\tQuarter Ending Dec - 18 [Mean, High, Low]")
print("\tStock price and Change [Stock, Percent]")
print("\t% Owned Institutions [Company, Industry, Sector]")
print("\tDividend Yield [Company, Industry, Sector]")

for company in companies_names.keys():
    company_name = companies_names[company]
    companyD = get_company_data_for_page(website_prefix + company)
    companies_data[company_name] = companyD
    print(company_name, companyD)






