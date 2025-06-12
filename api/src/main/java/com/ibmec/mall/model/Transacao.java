package com.ibmec.mall.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transacao")
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double valor;

    private String status; // "AUTHORIZED" ou "NOT_AUTHORIZED"

    private String codigoAutorizacao;

    private String mensagem;

    private LocalDateTime data;

    @ManyToOne
    @JoinColumn(name = "id_cartao")
    private CartaoCredito cartao;
}
