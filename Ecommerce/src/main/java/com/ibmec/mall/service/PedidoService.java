package com.ibmec.mall.service;

import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.ibmec.mall.model.Pedido;
import com.ibmec.mall.model.Produto;
import com.ibmec.mall.repository.PedidoRepository;
import com.ibmec.mall.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioService usuarioService;
    private final EventHubProducerClient eventHubProducerClient;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(String id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> buscarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Pedido criar(Pedido pedido) {
        // Verificar saldo do cartão
        if (!usuarioService.verificarSaldoSuficiente(pedido.getUsuarioId(), pedido.getValorTotal().doubleValue())) {
            throw new RuntimeException("Saldo insuficiente no cartão");
        }

        // Atualizar estoque dos produtos
        for (Pedido.ItemPedido item : pedido.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + item.getProdutoId()));
            
            if (produto.getQuantidadeEstoque() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }
            
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - item.getQuantidade());
            produtoRepository.save(produto);
        }

        // Configurar pedido
        pedido.setId(UUID.randomUUID().toString());
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus("PENDENTE");

        // Salvar pedido
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Enviar evento para análise
        eventHubProducerClient.createBatch()
            .addEvent(pedidoSalvo.toString())
            .send();

        return pedidoSalvo;
    }

    @Transactional
    public Pedido atualizarStatus(String id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return pedidoRepository.findByDataPedidoBetween(inicio, fim);
    }

    public List<Pedido> buscarPorStatus(String status) {
        return pedidoRepository.findByStatus(status);
    }
} 