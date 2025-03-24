package com.ibmec.mall.service;

import com.ibmec.mall.config.TestConfig;
import com.ibmec.mall.model.Pedido;
import com.ibmec.mall.model.Produto;
import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.repository.PedidoRepository;
import com.ibmec.mall.repository.ProdutoRepository;
import com.ibmec.mall.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
class EventHubServiceIntegrationTest {

    @Autowired
    private EventHubService eventHubService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Usuario usuario;
    private Produto produto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
        produtoRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha123");
        usuario.setEndereco("Endereço Teste");
        usuario.setTelefone("123456789");
        usuario = usuarioRepository.save(usuario);

        produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do produto teste");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setEstoque(10);
        produto.setCategoria("Eletrônicos");
        produto = produtoRepository.save(produto);

        pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus("PENDENTE");
        pedido.setTotal(new BigDecimal("100.00"));
        pedido.setProdutos(Arrays.asList(produto));
        pedido = pedidoRepository.save(pedido);
    }

    @Test
    void enviarEventoPedidoCriado_DeveEnviarEventoComSucesso() {
        assertDoesNotThrow(() -> eventHubService.enviarEventoPedidoCriado(pedido));
    }

    @Test
    void enviarEventoPedidoAtualizado_DeveEnviarEventoComSucesso() {
        pedido.setStatus("CONCLUIDO");
        pedido = pedidoRepository.save(pedido);

        assertDoesNotThrow(() -> eventHubService.enviarEventoPedidoAtualizado(pedido));
    }

    @Test
    void enviarEventoEstoqueBaixo_DeveEnviarEventoComSucesso() {
        produto.setEstoque(2);
        produto = produtoRepository.save(produto);

        assertDoesNotThrow(() -> eventHubService.enviarEventoEstoqueBaixo(produto));
    }

    @Test
    void enviarEventoVendaRealizada_DeveEnviarEventoComSucesso() {
        pedido.setStatus("CONCLUIDO");
        pedido = pedidoRepository.save(pedido);

        assertDoesNotThrow(() -> eventHubService.enviarEventoVendaRealizada(pedido));
    }
} 