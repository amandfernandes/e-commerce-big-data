import requests
from config import DefaultConfig

class ComprasAPI:
    def __init__(self):
        self.config = DefaultConfig()
        self.base_url = f"{self.config.URL_PREFIX}"  # URL da API Spring Boot

    def create_order(self, user_id, order_data: dict) -> dict:
        """
        Cria um novo pedido
        """
        try:
            response = requests.post(
                f"{self.base_url}/pedidos/{user_id}",
                json=order_data
            )
            
            if response.status_code in [200, 201]:
                return response.json()
            return {"status": "ERRO", "message": "Erro ao criar pedido"}
                
        except Exception as e:
            print(f"Erro ao criar pedido: {str(e)}")
            return {"status": "ERRO", "message": str(e)}

    def get_user_cards(self, user_id) -> list:
        """
        Busca os cartões do usuário
        """
        try:
            response = requests.get(f"{self.base_url}/cartoes/{user_id}")
            if response.status_code == 200:
                return response.json()
            print(f"Erro ao buscar cartões: Status {response.status_code}")
            print("DEBUG URL:", f"{self.base_url}/cartoes/{user_id}")
            return []
        except Exception as e:
            print(f"Erro ao buscar cartões: {str(e)}")
            return []

    def get_user_orders(self) -> dict:
        """
        Busca os pedidos do usuário
        """
        try:
            response = requests.get(f"{self.base_url}/pedidos/1/detalhes")
            
            if response.status_code == 200:
                pedidos = response.json()
                return {"data": pedidos}
                
            print(f"Erro ao buscar pedidos: Status {response.status_code}")
            print("DEBUG URL:", f"{self.base_url}/pedidos/1")
            return {"data": []}
                
        except Exception as e:
            print(f"Erro ao buscar pedidos: {str(e)}")
            return {"data": []}
