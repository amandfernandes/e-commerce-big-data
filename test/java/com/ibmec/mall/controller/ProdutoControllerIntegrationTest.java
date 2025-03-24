package com.ibmec.mall.controller;

import com.ibmec.mall.config.TestConfig;
import com.ibmec.mall.model.Produto;
import com.ibmec.mall.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
class ProdutoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        
        produto = new Produto();
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do produto teste");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setEstoque(10);
        produto.setCategoria("Eletrônicos");
        produto = produtoRepository.save(produto);
    }

    @Test
    void criarProduto_DeveRetornarProdutoCriado() throws Exception {
        Produto novoProduto = new Produto();
        novoProduto.setNome("Novo Produto");
        novoProduto.setDescricao("Nova descrição");
        novoProduto.setPreco(new BigDecimal("150.00"));
        novoProduto.setEstoque(5);
        novoProduto.setCategoria("Roupas");

        mockMvc.perform(post("/api/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Novo Produto\",\"descricao\":\"Nova descrição\",\"preco\":150.00,\"estoque\":5,\"categoria\":\"Roupas\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Produto")))
                .andExpect(jsonPath("$.preco", is(150.00)));
    }

    @Test
    void buscarPorId_DeveRetornarProduto() throws Exception {
        mockMvc.perform(get("/api/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(produto.getId().intValue())))
                .andExpect(jsonPath("$.nome", is(produto.getNome())))
                .andExpect(jsonPath("$.preco", is(produto.getPreco().doubleValue())));
    }

    @Test
    void listarTodos_DeveRetornarListaDeProdutos() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(produto.getId().intValue())))
                .andExpect(jsonPath("$[0].nome", is(produto.getNome())));
    }

    @Test
    void atualizarProduto_DeveRetornarProdutoAtualizado() throws Exception {
        mockMvc.perform(put("/api/produtos/{id}", produto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Produto Atualizado\",\"descricao\":\"Nova descrição\",\"preco\":200.00,\"estoque\":15,\"categoria\":\"Eletrônicos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(produto.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("Produto Atualizado")))
                .andExpect(jsonPath("$.preco", is(200.00)));
    }

    @Test
    void deletarProduto_DeveRetornarNoContent() throws Exception {
        mockMvc.perform(delete("/api/produtos/{id}", produto.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/produtos/{id}", produto.getId()))
                .andExpect(status().isNotFound());
    }
} 