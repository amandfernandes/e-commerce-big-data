package com.ibmec.mall.request;

import lombok.Data;

@Data
public class ItemPedidoRequest {
    private String produtoId;
    private Integer quantidade;
}
