# IBMEC-MALL

Sistema de E-commerce com Big Data na Nuvem desenvolvido com Spring Boot e Azure.

## Requisitos

- Java 17
- Maven
- Azure CLI
- Conta Azure com os seguintes serviços:
  - Azure App Service
  - Azure Cosmos DB
  - Azure SQL Database
  - Azure Event Hubs
  - Azure Data Factory

## Configuração do Ambiente

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/ibmec-mall.git
cd ibmec-mall
```

2. Configure as variáveis de ambiente no Azure:
```bash
export AZURE_SQL_URL="jdbc:sqlserver://seu-servidor.database.windows.net:1433;database=ibmec-mall-db"
export AZURE_SQL_USERNAME="seu-usuario"
export AZURE_SQL_PASSWORD="sua-senha"
export AZURE_COSMOS_ENDPOINT="https://seu-cosmos-account.documents.azure.com:443/"
export AZURE_COSMOS_KEY="sua-chave"
export AZURE_EVENTHUB_CONNECTION_STRING="Endpoint=sb://seu-eventhub.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=sua-chave"
```

3. Compile o projeto:
```bash
mvn clean package
```

4. Execute a aplicação:
```bash
java -jar target/ibmec-mall-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Produtos
- GET /api/produtos - Listar todos os produtos
- GET /api/produtos/{id} - Buscar produto por ID
- POST /api/produtos - Criar novo produto
- PUT /api/produtos/{id} - Atualizar produto
- DELETE /api/produtos/{id} - Remover produto
- GET /api/produtos/categoria/{categoria} - Listar produtos por categoria
- GET /api/produtos/ativos - Listar produtos ativos

### Usuários
- GET /api/usuarios - Listar todos os usuários
- GET /api/usuarios/{id} - Buscar usuário por ID
- POST /api/usuarios - Criar novo usuário
- PUT /api/usuarios/{id} - Atualizar usuário
- DELETE /api/usuarios/{id} - Remover usuário
- PUT /api/usuarios/{id}/cartao/saldo - Atualizar saldo do cartão

### Pedidos
- GET /api/pedidos - Listar todos os pedidos
- GET /api/pedidos/{id} - Buscar pedido por ID
- POST /api/pedidos - Criar novo pedido
- PUT /api/pedidos/{id}/status - Atualizar status do pedido
- GET /api/pedidos/usuario/{usuarioId} - Buscar pedidos por usuário
- GET /api/pedidos/periodo - Buscar pedidos por período
- GET /api/pedidos/status/{status} - Buscar pedidos por status

## Documentação da API

A documentação da API está disponível através do Swagger UI em:
```
http://localhost:8080/swagger-ui.html
```

## Pipeline de Big Data

O sistema utiliza o seguinte pipeline para análise de dados:

1. **Ingestão**: Os dados de vendas são enviados para o Azure Event Hubs
2. **Processamento**: O Azure Data Factory processa os dados
3. **Visualização**: Os dados são visualizados no Power BI

## Deploy

O deploy é automatizado através do GitHub Actions quando há push na branch main.

## Testes

Para executar os testes:
```bash
mvn test
```

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes. 