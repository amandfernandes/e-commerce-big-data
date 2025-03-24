package com.ibmec.mall.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String email;
    private String senha;
    
    @Embedded
    private CartaoCredito cartaoCredito;
    
    @Data
    @Embeddable
    public static class CartaoCredito {
        private String numero;
        private String titular;
        private String dataValidade;
        private String cvv;
        private BigDecimal saldo;
    }
} 