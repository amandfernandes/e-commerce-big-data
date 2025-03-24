-- Inserir usuários de teste
INSERT INTO usuario (id, nome, email, senha, endereco, telefone) VALUES
(1, 'Usuário Teste 1', 'teste1@email.com', 'senha123', 'Endereço 1', '123456789'),
(2, 'Usuário Teste 2', 'teste2@email.com', 'senha456', 'Endereço 2', '987654321');

-- Inserir produtos de teste
INSERT INTO produto (id, nome, descricao, preco, estoque, categoria) VALUES
(1, 'Produto Teste 1', 'Descrição do produto 1', 100.00, 10, 'Eletrônicos'),
(2, 'Produto Teste 2', 'Descrição do produto 2', 200.00, 5, 'Roupas'),
(3, 'Produto Teste 3', 'Descrição do produto 3', 150.00, 8, 'Livros');

-- Inserir pedidos de teste
INSERT INTO pedido (id, usuario_id, data_pedido, status, total) VALUES
(1, 1, CURRENT_TIMESTAMP(), 'PENDENTE', 100.00),
(2, 2, CURRENT_TIMESTAMP(), 'CONCLUIDO', 200.00);

-- Inserir itens de pedido de teste
INSERT INTO pedido_produtos (pedido_id, produtos_id) VALUES
(1, 1),
(2, 2); 