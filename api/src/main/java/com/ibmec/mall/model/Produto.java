package com.ibmec.mall.model;

import org.springframework.data.annotation.Id;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.Data;
import java.util.List;

@Data
@Container(containerName = "products")
public class Produto {

    @Id
    private String id;

    @PartitionKey
    private String categoria;

    private String nome;
    private double preco;
    private String descricao;
    private List<String> imagens;
}

