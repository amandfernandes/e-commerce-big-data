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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
class CosmosDBServiceIntegrationTest {

    @Autowired
    private CosmosDBService cosmosDBService;

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
    void salvarPedido_DeveSalvarNoCosmosDB() {
        assertDoesNotThrow(() -> cosmosDBService.salvarPedido(pedido));
    }

    @Test
    void buscarPedidoPorId_DeveRetornarPedido() {
        cosmosDBService.salvarPedido(pedido);
        Pedido pedidoEncontrado = cosmosDBService.buscarPedidoPorId(pedido.getId());

        assertNotNull(pedidoEncontrado);
        assertEquals(pedido.getId(), pedidoEncontrado.getId());
        assertEquals(pedido.getUsuario().getId(), pedidoEncontrado.getUsuario().getId());
        assertEquals(pedido.getTotal(), pedidoEncontrado.getTotal());
    }

    @Test
    void listarPedidosPorUsuario_DeveRetornarListaDePedidos() {
        cosmosDBService.salvarPedido(pedido);
        List<Pedido> pedidos = cosmosDBService.listarPedidosPorUsuario(usuario.getId());

        assertNotNull(pedidos);
        assertFalse(pedidos.isEmpty());
        assertTrue(pedidos.stream().allMatch(p -> p.getUsuario().getId().equals(usuario.getId())));
    }

    @Test
    void listarPedidosPorPeriodo_DeveRetornarListaDePedidos() {
        cosmosDBService.salvarPedido(pedido);
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);

        List<Pedido> pedidos = cosmosDBService.listarPedidosPorPeriodo(inicio, fim);

        assertNotNull(pedidos);
        assertFalse(pedidos.isEmpty());
        assertTrue(pedidos.stream().allMatch(p -> 
            p.getDataPedido().isAfter(inicio) && p.getDataPedido().isBefore(fim)));
    }

    @Test
    void atualizarStatusPedido_DeveAtualizarNoCosmosDB() {
        cosmosDBService.salvarPedido(pedido);
        pedido.setStatus("CONCLUIDO");
        pedido = pedidoRepository.save(pedido);

        assertDoesNotThrow(() -> cosmosDBService.atualizarStatusPedido(pedido.getId(), "CONCLUIDO"));

        Pedido pedidoAtualizado = cosmosDBService.buscarPedidoPorId(pedido.getId());
        assertNotNull(pedidoAtualizado);
        assertEquals("CONCLUIDO", pedidoAtualizado.getStatus());
    }
} 