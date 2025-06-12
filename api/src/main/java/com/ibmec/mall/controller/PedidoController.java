package com.ibmec.mall.controller;

import com.ibmec.mall.model.*;
import com.ibmec.mall.repository.*;
import com.ibmec.mall.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/pedidos/{id_usuario}")
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @PostMapping
    public ResponseEntity<Pedido> create(@PathVariable("id_usuario") Integer idUsuario,
                                       @RequestBody PedidoRequest request) {
        // Valida usuário
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idUsuario);
        if (optionalUsuario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Valida cartão
        Optional<CartaoCredito> optionalCartao = cartaoCreditoRepository.findById(request.getIdCartao());
        if (optionalCartao.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(optionalUsuario.get());
        pedido.setCartaoCredito(optionalCartao.get());
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus("PENDENTE");

        List<ItemPedido> itens = new ArrayList<>();
        double valorTotal = 0;

        // Processa os itens do pedido
        for (ItemPedidoRequest itemRequest : request.getItens()) {
            Optional<Produto> optionalProduto = produtoRepository.findById(itemRequest.getProdutoId());
            if (optionalProduto.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Produto produto = optionalProduto.get();
            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProdutoId(produto.getId());
            item.setNomeProduto(produto.getNome());
            item.setQuantidade(itemRequest.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco() * itemRequest.getQuantidade());

            itens.add(item);
            valorTotal += item.getSubtotal();
        }

        pedido.setItens(itens);
        pedido.setValorTotal(valorTotal);

        // Processa o pagamento
        TransacaoRequest transacaoRequest = new TransacaoRequest();
        transacaoRequest.setNumero(pedido.getCartaoCredito().getNumero());
        transacaoRequest.setCvv(pedido.getCartaoCredito().getCvv());
        transacaoRequest.setValor(valorTotal);

        // Salva o pedido
        pedidoRepository.save(pedido);
        
        return new ResponseEntity<>(pedido, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listByUser(@PathVariable("id_usuario") Integer idUsuario) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(idUsuario);
        if (optionalUsuario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Pedido> pedidos = pedidoRepository.findByUsuario(optionalUsuario.get());
        return new ResponseEntity<>(pedidos, HttpStatus.OK);
    }

    @GetMapping("/{id_pedido}")
    public ResponseEntity<Pedido> getById(@PathVariable("id_usuario") Integer idUsuario,
                                        @PathVariable("id_pedido") Integer idPedido) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(idPedido);
        if (optionalPedido.isEmpty() || !optionalPedido.get().getUsuario().getId().equals(idUsuario)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(optionalPedido.get(), HttpStatus.OK);
    }
}
