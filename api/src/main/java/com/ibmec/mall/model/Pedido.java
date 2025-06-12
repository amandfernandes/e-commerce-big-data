package com.ibmec.mall.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens;

    private LocalDateTime dataPedido;
    private String status; // PENDENTE, APROVADO, REJEITADO
    private Double valorTotal;

    @ManyToOne
    @JoinColumn(name = "id_cartao")
    private CartaoCredito cartaoCredito;
}
