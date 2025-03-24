package com.ibmec.mall.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
class RelatorioVendasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
        pedido.setStatus("CONCLUIDO");
        pedido.setTotal(new BigDecimal("100.00"));
        pedido.setProdutos(Arrays.asList(produto));
        pedido = pedidoRepository.save(pedido);
    }

    @Test
    void gerarRelatorioVendas_DeveRetornarRelatorioCompleto() throws Exception {
        mockMvc.perform(get("/api/relatorios/vendas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$[0].total", is(pedido.getTotal().doubleValue())));
    }

    @Test
    void gerarRelatorioVendasPorPeriodo_DeveRetornarPedidosDoPeriodo() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/api/relatorios/vendas/periodo")
                .param("inicio", inicio.toString())
                .param("fim", fim.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pedido.getId().intValue())));
    }

    @Test
    void gerarRelatorioVendasPorCategoria_DeveRetornarPedidosPorCategoria() throws Exception {
        mockMvc.perform(get("/api/relatorios/vendas/categoria/{categoria}", produto.getCategoria()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].produtos[0].categoria", is(produto.getCategoria())));
    }

    @Test
    void gerarRelatorioVendasPorUsuario_DeveRetornarPedidosDoUsuario() throws Exception {
        mockMvc.perform(get("/api/relatorios/vendas/usuario/{usuarioId}", usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].usuario.id", is(usuario.getId().intValue())));
    }

    @Test
    void gerarRelatorioVendasPorPeriodo_ComPeriodoInvalido_DeveRetornarBadRequest() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = LocalDateTime.now().minusDays(1);

        mockMvc.perform(get("/api/relatorios/vendas/periodo")
                .param("inicio", inicio.toString())
                .param("fim", fim.toString()))
                .andExpect(status().isBadRequest());
    }
} 