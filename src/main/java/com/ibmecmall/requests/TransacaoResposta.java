package com.ibmecmall.requests;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransacaoResposta {
    private String status;
    private UUID codigo;
    private LocalDateTime dtTransacao;
    private String messagem;
}
