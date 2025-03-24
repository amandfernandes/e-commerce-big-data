package com.ibmec.mall.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.ibmec.mall.model.Pedido;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends CosmosRepository<Pedido, String> {
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Pedido> findByStatus(String status);
} 