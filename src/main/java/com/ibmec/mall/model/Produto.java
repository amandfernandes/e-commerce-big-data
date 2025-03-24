package com.ibmec.mall.model;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Data
@Container(containerName = "produtos")
public class Produto {
    @Id
    private String id;
    
    @PartitionKey
    private String categoria;
    
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;
    private String imagemUrl;
    private boolean ativo;
} 