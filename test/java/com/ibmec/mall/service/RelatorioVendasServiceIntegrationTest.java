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
class RelatorioVendasServiceIntegrationTest {

    @Autowired
    private RelatorioVendasService relatorioVendasService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Usuario usuario;
    private Produto produto1;
    private Produto produto2;
    private Pedido pedido1;
    private Pedido pedido2;

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

        produto1 = new Produto();
        produto1.setNome("Produto 1");
        produto1.setDescricao("Descrição do produto 1");
        produto1.setPreco(new BigDecimal("100.00"));
        produto1.setEstoque(10);
        produto1.setCategoria("Eletrônicos");
        produto1 = produtoRepository.save(produto1);

        produto2 = new Produto();
        produto2.setNome("Produto 2");
        produto2.setDescricao("Descrição do produto 2");
        produto2.setPreco(new BigDecimal("200.00"));
        produto2.setEstoque(5);
        produto2.setCategoria("Roupas");
        produto2 = produtoRepository.save(produto2);

        pedido1 = new Pedido();
        pedido1.setUsuario(usuario);
        pedido1.setDataPedido(LocalDateTime.now().minusDays(1));
        pedido1.setStatus("CONCLUIDO");
        pedido1.setTotal(new BigDecimal("100.00"));
        pedido1.setProdutos(Arrays.asList(produto1));
        pedido1 = pedidoRepository.save(pedido1);

        pedido2 = new Pedido();
        pedido2.setUsuario(usuario);
        pedido2.setDataPedido(LocalDateTime.now());
        pedido2.setStatus("CONCLUIDO");
        pedido2.setTotal(new BigDecimal("200.00"));
        pedido2.setProdutos(Arrays.asList(produto2));
        pedido2 = pedidoRepository.save(pedido2);
    }

    @Test
    void gerarRelatorioVendas_DeveRetornarRelatorioCompleto() {
        List<Pedido> pedidos = relatorioVendasService.gerarRelatorioVendas();

        assertNotNull(pedidos);
        assertEquals(2, pedidos.size());

        BigDecimal totalVendas = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("300.00"), totalVendas);

        long totalProdutos = pedidos.stream()
                .mapToLong(p -> p.getProdutos().size())
                .sum();
        assertEquals(2, totalProdutos);
    }

    @Test
    void gerarRelatorioVendasPorPeriodo_DeveRetornarPedidosDoPeriodo() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);

        List<Pedido> pedidos = relatorioVendasService.gerarRelatorioVendasPorPeriodo(inicio, fim);

        assertNotNull(pedidos);
        assertEquals(2, pedidos.size());
    }

    @Test
    void gerarRelatorioVendasPorCategoria_DeveRetornarPedidosPorCategoria() {
        List<Pedido> pedidosEletronicos = relatorioVendasService.gerarRelatorioVendasPorCategoria("Eletrônicos");
        List<Pedido> pedidosRoupas = relatorioVendasService.gerarRelatorioVendasPorCategoria("Roupas");

        assertNotNull(pedidosEletronicos);
        assertNotNull(pedidosRoupas);
        assertEquals(1, pedidosEletronicos.size());
        assertEquals(1, pedidosRoupas.size());
        assertEquals(produto1.getCategoria(), pedidosEletronicos.get(0).getProdutos().get(0).getCategoria());
        assertEquals(produto2.getCategoria(), pedidosRoupas.get(0).getProdutos().get(0).getCategoria());
    }

    @Test
    void gerarRelatorioVendasPorUsuario_DeveRetornarPedidosDoUsuario() {
        List<Pedido> pedidos = relatorioVendasService.gerarRelatorioVendasPorUsuario(usuario.getId());

        assertNotNull(pedidos);
        assertEquals(2, pedidos.size());
        assertTrue(pedidos.stream().allMatch(p -> p.getUsuario().getId().equals(usuario.getId())));
    }
} 