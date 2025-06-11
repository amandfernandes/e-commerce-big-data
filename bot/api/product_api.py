import requests
from config import DefaultConfig

class ProductAPI:
    def __init__(self):
        self.config = DefaultConfig()
        self.base_url = f"{self.config.URL_PREFIX}/produtos"

    def get_products(self):
        response = requests.get(self.base_url)
        if response.status_code == 200:
            return response.json()
        else:
            return None

    def search_product(self, product_name):
        response = requests.get(f"{self.base_url}", params={"nome": product_name})
        if response.status_code == 200:
            return response.json()
        else:
            return None    
    
