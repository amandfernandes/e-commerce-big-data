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
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Pedido pedido;
    private Usuario usuario;
    private Produto produto;

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
    void criarPedido_DeveRetornarPedidoCriado() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":{\"id\":" + usuario.getId() + "},\"produtos\":[{\"id\":" + produto.getId() + "}],\"total\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.usuario.id", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$.total", is(100.00)));
    }

    @Test
    void buscarPorId_DeveRetornarPedido() throws Exception {
        mockMvc.perform(get("/api/pedidos/{id}", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$.usuario.id", is(usuario.getId().intValue())))
                .andExpect(jsonPath("$.total", is(pedido.getTotal().doubleValue())));
    }

    @Test
    void listarTodos_DeveRetornarListaDePedidos() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$[0].usuario.id", is(usuario.getId().intValue())));
    }

    @Test
    void atualizarStatus_DeveRetornarPedidoAtualizado() throws Exception {
        mockMvc.perform(put("/api/pedidos/{id}/status", pedido.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"CONCLUIDO\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedido.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CONCLUIDO")));
    }

    @Test
    void deletarPedido_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/pedidos/{id}", pedido.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pedidos/{id}", pedido.getId()))
                .andExpect(status().isNotFound());
    }
} 