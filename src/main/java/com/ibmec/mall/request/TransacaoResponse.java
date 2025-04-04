package com.ibmec.mall.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransacaoResponse {
    private String status;             // Aprovado / Reprovado
    private LocalDateTime data;       // Data da transação
    private String codigoAutorizacao; // Código aleatório de confirmação
    private String mensagem;          // Motivo se for reprovado
}