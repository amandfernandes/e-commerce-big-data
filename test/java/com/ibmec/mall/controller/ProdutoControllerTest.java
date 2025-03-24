package com.ibmec.mall.controller;

import com.ibmec.mall.model.Produto;
import com.ibmec.mall.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ProdutoController produtoController;

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setDescricao("Descrição do produto teste");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setEstoque(10);
        produto.setCategoria("Eletrônicos");
    }

    @Test
    void criarProduto_DeveRetornarProdutoCriado() {
        when(produtoService.criarProduto(any(Produto.class))).thenReturn(produto);

        ResponseEntity<Produto> response = produtoController.criarProduto(produto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(produto.getId(), response.getBody().getId());
        assertEquals(produto.getNome(), response.getBody().getNome());
        verify(produtoService).criarProduto(produto);
    }

    @Test
    void buscarPorId_DeveRetornarProduto() {
        when(produtoService.buscarPorId(1L)).thenReturn(produto);

        ResponseEntity<Produto> response = produtoController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(produto.getId(), response.getBody().getId());
        assertEquals(produto.getNome(), response.getBody().getNome());
        verify(produtoService).buscarPorId(1L);
    }

    @Test
    void listarTodos_DeveRetornarListaDeProdutos() {
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoService.listarTodos()).thenReturn(produtos);

        ResponseEntity<List<Produto>> response = produtoController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(produto.getId(), response.getBody().get(0).getId());
        verify(produtoService).listarTodos();
    }

    @Test
    void atualizarProduto_DeveRetornarProdutoAtualizado() {
        when(produtoService.atualizarProduto(eq(1L), any(Produto.class))).thenReturn(produto);

        ResponseEntity<Produto> response = produtoController.atualizarProduto(1L, produto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(produto.getId(), response.getBody().getId());
        assertEquals(produto.getNome(), response.getBody().getNome());
        verify(produtoService).atualizarProduto(1L, produto);
    }

    @Test
    void deletarProduto_DeveRetornarNoContent() {
        ResponseEntity<Void> response = produtoController.deletarProduto(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(produtoService).deletarProduto(1L);
    }
} 