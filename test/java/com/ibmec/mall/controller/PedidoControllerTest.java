package com.ibmec.mall.controller;

import com.ibmec.mall.model.Pedido;
import com.ibmec.mall.model.Produto;
import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    private Pedido pedido;
    private Usuario usuario;
    private Produto produto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");

        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setEstoque(10);

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuario(usuario);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus("PENDENTE");
        pedido.setTotal(new BigDecimal("100.00"));
        pedido.setProdutos(Arrays.asList(produto));
    }

    @Test
    void criarPedido_DeveRetornarPedidoCriado() {
        when(pedidoService.criarPedido(any(Pedido.class))).thenReturn(pedido);

        ResponseEntity<Pedido> response = pedidoController.criarPedido(pedido);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pedido.getId(), response.getBody().getId());
        assertEquals(pedido.getUsuario().getId(), response.getBody().getUsuario().getId());
        assertEquals(pedido.getTotal(), response.getBody().getTotal());
        verify(pedidoService).criarPedido(pedido);
    }

    @Test
    void buscarPorId_DeveRetornarPedido() {
        when(pedidoService.buscarPorId(1L)).thenReturn(pedido);

        ResponseEntity<Pedido> response = pedidoController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pedido.getId(), response.getBody().getId());
        assertEquals(pedido.getUsuario().getId(), response.getBody().getUsuario().getId());
        assertEquals(pedido.getTotal(), response.getBody().getTotal());
        verify(pedidoService).buscarPorId(1L);
    }

    @Test
    void listarTodos_DeveRetornarListaDePedidos() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoService.listarTodos()).thenReturn(pedidos);

        ResponseEntity<List<Pedido>> response = pedidoController.listarTodos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(pedido.getId(), response.getBody().get(0).getId());
        assertEquals(pedido.getUsuario().getId(), response.getBody().get(0).getUsuario().getId());
        verify(pedidoService).listarTodos();
    }

    @Test
    void atualizarStatus_DeveRetornarPedidoAtualizado() {
        when(pedidoService.atualizarStatus(1L, "CONCLUIDO")).thenReturn(pedido);

        ResponseEntity<Pedido> response = pedidoController.atualizarStatus(1L, "CONCLUIDO");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(pedido.getId(), response.getBody().getId());
        assertEquals(pedido.getStatus(), response.getBody().getStatus());
        verify(pedidoService).atualizarStatus(1L, "CONCLUIDO");
    }

    @Test
    void deletarPedido_DeveRetornarNoContent() {
        ResponseEntity<Void> response = pedidoController.deletarPedido(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(pedidoService).deletarPedido(1L);
    }
} 