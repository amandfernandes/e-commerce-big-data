package com.ibmec.mall.request;

import lombok.Data;
import java.util.List;

@Data
public class PedidoRequest {
    private Integer idCartao;
    private List<ItemPedidoRequest> itens;
}
