# Bot Framework para E-commerce | Projeto IBMEC-MALL

Este projeto consiste no desenvolvimento de um **bot em Python** que serve como interface conversacional para o e-commerce **IBMEC-MALL**. O bot se conecta à API do projeto para melhorar a experiência do usuário, facilitar compras e consultas.

O trabalho foi desenvolvido para a disciplina de Big Data e Cloud Computing, integrando-se a uma arquitetura robusta baseada em nuvem.

---

## 👥 Integrantes

* **Amanda Fernandes** - **202309924901**
* **Gabriel Monteiro** - **202301135052**
* **João Gabriel Rodrigez** - **202307545333**

---

## 🏛️ Arquitetura do E-commerce (IBMEC-MALL)

O bot interage com um ecossistema de e-commerce completo, implantado no **Microsoft Azure**. A arquitetura subjacente inclui:

* **API Gateway (Azure API Management):** Ponto central que gerencia e roteia todas as requisições para os serviços de backend.
* **Backend (Azure App Service):** Aplicação principal que contém a lógica de negócio para gerenciar produtos, usuários e pedidos. Valida o saldo do cartão de crédito antes de confirmar as transações.
* **Banco de Dados NoSQL (Azure Cosmos DB):** Armazena dados de produtos e pedidos, oferecendo alta escalabilidade.
* **Banco de Dados Relacional (Azure SQL Database):** Gerencia os dados dos usuários e suas informações de cartão de crédito.
* **Pipeline de Big Data:**
    * **Ingestão:** Azure Event Hubs captura os dados de vendas em tempo real.
    * **Processamento:** Azure Data Factory transforma os dados para análise.
    * **Visualização:** Power BI gera relatórios e dashboards para os administradores.
* **CI/CD (GitHub Actions):** Automação completa do processo de deploy, garantindo entregas rápidas e seguras.

### Endpoints da API

O bot utiliza os seguintes endpoints para se comunicar com o backend:

* `POST /usuario`: Criar um novo usuário com cartão de crédito.
* `GET /produtos`: Listar todos os produtos disponíveis.
* `POST /pedido`: Criar um novo pedido para um usuário.
* E endpoints para gestão de produtos (`POST`, `PUT`, `DELETE /produto/{id}`).

---

## ✨ Funcionalidades do Bot

Utilizando a API do IBMEC-MALL, nosso bot oferece as seguintes funcionalidades:

* **Busca de Produtos:** Permite que os usuários listem os produtos do catálogo.
* **Realização de Pedidos:** Guia o usuário no processo de criação de um pedido.
* **Ver exrato:** Permite que o usuario vizulize o extrato de de suas compras
* **Realizar compra:** Permite que Realizar compra de produtos.
---

## 🚀 Instalação e Execução do Bot

Para executar o bot em um ambiente de desenvolvimento, siga os passos abaixo.

### Pré-requisitos

* Python 3.9 ou superior
* `pip` (gerenciador de pacotes do Python)
* Git

### Passos

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/amandfernandes/e-commerce-big-data.git
    cd e-commerce-big-data
    ```

2.  **Crie e ative um ambiente virtual:**
    ```bash
    # Para Linux/macOS
    python3 -m venv venv
    source venv/bin/activate

    # Para Windows
    python -m venv venv
    .\venv\Scripts\activate
    ```

3.  **Instale as dependências:**
    ```bash
    pip install -r requirements.txt
    ```

4.  **Configure as variáveis de ambiente:**
    * Renomeie o arquivo `.env.example` para `.env`.
    * Adicione as chaves necessárias, como o token do bot e a URL do API Gateway do Azure.


5.  **Execute o bot:**
  ```bash
    cd bot
  ```
   
   ```bash
    python main.py
   ```

---

## 🤖 Como Usar

Após iniciar o bot, interaja com ele através do chat onde o usuario pode vizualizar as opções disponíveis sendo elas "FAZER PEDIDOS", "BUSCAR PRODUTOS", "MEU EXTRATO","FAZER COMPRA".
