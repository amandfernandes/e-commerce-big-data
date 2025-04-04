package com.ibmec.mall.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransacaoRequest {
    private String numero;
    private LocalDateTime dtExpiracao;
    private String cvv;
    private Double valor;
    private Integer idUsuario; // pra validar o dono do cartão
}