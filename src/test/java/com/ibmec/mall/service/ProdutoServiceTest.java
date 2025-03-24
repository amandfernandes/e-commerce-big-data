package com.ibmec.mall.service;

import com.ibmec.mall.model.Produto;
import com.ibmec.mall.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

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
    void criarProduto_DeveRetornarProdutoSalvo() {
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        Produto resultado = produtoService.criarProduto(produto);

        assertNotNull(resultado);
        assertEquals(produto.getId(), resultado.getId());
        assertEquals(produto.getNome(), resultado.getNome());
        assertEquals(produto.getPreco(), resultado.getPreco());
        verify(produtoRepository).save(produto);
    }

    @Test
    void buscarPorId_QuandoProdutoExiste_DeveRetornarProduto() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        Produto resultado = produtoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(produto.getId(), resultado.getId());
        assertEquals(produto.getNome(), resultado.getNome());
    }

    @Test
    void buscarPorId_QuandoProdutoNaoExiste_DeveLancarExcecao() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> produtoService.buscarPorId(1L));
    }

    @Test
    void listarTodos_DeveRetornarListaDeProdutos() {
        List<Produto> produtos = Arrays.asList(produto);
        when(produtoRepository.findAll()).thenReturn(produtos);

        List<Produto> resultado = produtoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(produto.getId(), resultado.get(0).getId());
    }

    @Test
    void atualizarProduto_QuandoProdutoExiste_DeveRetornarProdutoAtualizado() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        Produto resultado = produtoService.atualizarProduto(1L, produto);

        assertNotNull(resultado);
        assertEquals(produto.getId(), resultado.getId());
        assertEquals(produto.getNome(), resultado.getNome());
        verify(produtoRepository).save(produto);
    }

    @Test
    void atualizarProduto_QuandoProdutoNaoExiste_DeveLancarExcecao() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> produtoService.atualizarProduto(1L, produto));
    }

    @Test
    void deletarProduto_DeveChamarRepository() {
        produtoService.deletarProduto(1L);
        verify(produtoRepository).deleteById(1L);
    }
} 