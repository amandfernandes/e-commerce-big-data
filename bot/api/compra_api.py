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
        url = f"{self.base_url}/pedidos/{user_id}"
        print(f"\n--- INICIANDO POST PARA A API ---")
        print(f"URL: {url}")
        print(f"DADOS (JSON): {order_data}")

        try:
            response = requests.post(url, json=order_data)
            
            print(f"STATUS DA RESPOSTA DA API: {response.status_code}")
            # Usamos repr() para ver caracteres especiais como quebras de linha (\n)
            print(f"CONTEÚDO DA RESPOSTA (RAW TEXT): >>>{repr(response.text)}<<<")
            print(f"--- FIM DA RESPOSTA DA API ---\n")

            # Se o status code não for de sucesso, já sabemos que é um erro.
            if response.status_code not in [200, 201]:
                return {"status": "ERRO", "message": f"API retornou status {response.status_code}: {response.text}"}

            # Se o status for de sucesso, mas a resposta estiver vazia...
            if not response.text:
                print("AVISO: Resposta de sucesso da API está vazia. Verifique o método no Spring Boot.")
                # Retorna um JSON de sucesso genérico para o bot não quebrar.
                return {"id": "processado_sem_retorno_da_api", "status": "SUCESSO"}

            # Se tiver conteúdo, tenta fazer o parse do JSON.
            return response.json()
                
        except ValueError:
            # Este erro acontece se response.text não for um JSON válido
            print(f"ERRO DE JSON: A resposta da API (status {response.status_code}) não é um JSON válido.")
            return {"status": "ERRO", "message": "A resposta da API não estava no formato JSON esperado."}
        except Exception as e:
            print(f"ERRO DE CONEXÃO ao criar pedido: {str(e)}")
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

    def get_user_orders(self, user_id) -> dict:
        """
        Busca os pedidos do usuário
        """
        try:
            response = requests.get(f"{self.base_url}/pedidos/{user_id}/detalhes")
            
            if response.status_code == 200:
                pedidos = response.json()
                return {"data": pedidos}
                
            print(f"Erro ao buscar pedidos: Status {response.status_code}")
            print("DEBUG URL:", f"{self.base_url}/pedidos/{user_id}")
            return {"data": []}
                
        except Exception as e:
            print(f"Erro ao buscar pedidos: {str(e)}")
            return {"data": []}
