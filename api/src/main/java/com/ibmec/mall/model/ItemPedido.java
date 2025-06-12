package com.ibmec.mall.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "itens_pedido")
public class ItemPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    private String produtoId; // ID do produto no Cosmos DB
    private String nomeProduto;
    private Integer quantidade;
    private Double precoUnitario;
    private Double subtotal;
}
