import requests
from config import DefaultConfig

class ComprasAPI:
    def __init__(self):
        self.config = DefaultConfig()
        self.base_url = f"{self.config.URL_PREFIX}"

    def create_order(self, user_id, cart_items, payment_info):
        """Cria um novo pedido"""
        try:
            order_data = {
                "user_id": user_id,
                "items": cart_items,
                "payment_method": payment_info["method"],
                "payment_details": payment_info.get("details", {}),
                "total": sum(item["price"] * item["quantity"] for item in cart_items)
            }
            
            response = requests.post(f"{self.base_url}/pedidos", json=order_data)
            if response.status_code == 201:
                return response.json()
            return None
        except Exception as e:
            print(f"Erro ao criar pedido: {e}")
            return None

    def add_to_cart(self, user_id, product_id, quantity):
        try:
            cart_data = {
                "product_id": product_id,
                "quantity": quantity
            }
            
            response = requests.post(f"{self.base_url}/carrinho/{user_id}", json=cart_data)
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"Erro ao adicionar ao carrinho: {e}")
            return None

    def get_user_orders(self, user_id):
        """Obtém os pedidos de um usuário"""
        try:
            response = requests.get(f"{self.base_url}/pedidos/usuario/{user_id}")
            if response.status_code == 200:
                return response.json()
            return []
        except Exception as e:
            print(f"Erro ao obter pedidos do usuário: {e}")
            return []

    def get_order_by_id(self, order_id):
        """Obtém um pedido específico pelo ID"""
        try:
            response = requests.get(f"{self.base_url}/pedidos/{order_id}")
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"Erro ao obter pedido por ID: {e}")
            return None

