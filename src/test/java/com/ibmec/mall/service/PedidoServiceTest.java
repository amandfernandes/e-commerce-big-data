package com.ibmec.mall.service;

import com.ibmec.mall.model.Pedido;
import com.ibmec.mall.model.Produto;
import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.repository.PedidoRepository;
import com.ibmec.mall.repository.ProdutoRepository;
import com.ibmec.mall.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private PedidoService pedidoService;

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
    void criarPedido_QuandoUsuarioEProdutosExistem_DeveRetornarPedidoSalvo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        Pedido resultado = pedidoService.criarPedido(pedido);

        assertNotNull(resultado);
        assertEquals(pedido.getId(), resultado.getId());
        assertEquals(pedido.getUsuario().getId(), resultado.getUsuario().getId());
        assertEquals(pedido.getTotal(), resultado.getTotal());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void criarPedido_QuandoUsuarioNaoExiste_DeveLancarExcecao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pedidoService.criarPedido(pedido));
    }

    @Test
    void buscarPorId_QuandoPedidoExiste_DeveRetornarPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Pedido resultado = pedidoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(pedido.getId(), resultado.getId());
        assertEquals(pedido.getUsuario().getId(), resultado.getUsuario().getId());
    }

    @Test
    void buscarPorId_QuandoPedidoNaoExiste_DeveLancarExcecao() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pedidoService.buscarPorId(1L));
    }

    @Test
    void listarTodos_DeveRetornarListaDePedidos() {
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        List<Pedido> resultado = pedidoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(pedido.getId(), resultado.get(0).getId());
    }

    @Test
    void atualizarStatus_QuandoPedidoExiste_DeveRetornarPedidoAtualizado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        Pedido resultado = pedidoService.atualizarStatus(1L, "CONCLUIDO");

        assertNotNull(resultado);
        assertEquals("CONCLUIDO", resultado.getStatus());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void atualizarStatus_QuandoPedidoNaoExiste_DeveLancarExcecao() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pedidoService.atualizarStatus(1L, "CONCLUIDO"));
    }

    @Test
    void deletarPedido_DeveChamarRepository() {
        pedidoService.deletarPedido(1L);
        verify(pedidoRepository).deleteById(1L);
    }
} 