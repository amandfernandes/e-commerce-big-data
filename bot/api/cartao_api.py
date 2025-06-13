import requests
from config import DefaultConfig

class CartaoAPI:
    def __init__(self):
        self.config = DefaultConfig()
        self.base_url = f"{self.config.URL_PREFIX}"

    def user_exists(self, user_id):
        """
        Verifica se o usuário existe verificando se ele possui cartões
        """
        try:
            response = requests.get(f"{self.base_url}/cartoes/{user_id}")
            return response.status_code == 200
        except Exception:
            return False

    def get_user_cards(self, user_id):
        try:
            response = requests.get(f"{self.base_url}/cartoes/{user_id}")
            if response.status_code == 200:
                return response.json()
            return []
        except Exception as e:
            print(f"Erro ao buscar cartões: {e}")
            return []

    def get_card_statement(self, user_id, card_id, start_date=None, end_date=None):
        try:
            params = {}
            if start_date:
                params['dataInicio'] = start_date
            if end_date:
                params['dataFim'] = end_date

            response = requests.get(
                f"{self.base_url}/cartoes/{user_id}/{card_id}/extrato", 
                params=params
            )
            
            if response.status_code == 200:
                return response.json()
            print(f"Erro ao buscar extrato: Status {response.status_code}")
            return []
        except Exception as e:
            print(f"Erro ao buscar extrato: {e}")
            return []