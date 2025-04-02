package com.ibmecmall.requests;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransacaoRequest {
    private String numero;
    private LocalDateTime dtExpiracao;
    private String cvv;
    private Double valor;
}