package com.ibmec.mall.repository;

import com.ibmec.mall.model.Produto;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends CosmosRepository<Produto, String> {
}
