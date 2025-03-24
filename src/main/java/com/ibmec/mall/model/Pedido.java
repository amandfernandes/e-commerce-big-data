package com.ibmec.mall.model;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Container(containerName = "pedidos")
public class Pedido {
    @Id
    private String id;
    
    @PartitionKey
    private Long usuarioId;
    
    private LocalDateTime dataPedido;
    private BigDecimal valorTotal;
    private String status;
    private List<ItemPedido> itens;
    
    @Data
    public static class ItemPedido {
        private String produtoId;
        private String nomeProduto;
        private Integer quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal subtotal;
    }
} 