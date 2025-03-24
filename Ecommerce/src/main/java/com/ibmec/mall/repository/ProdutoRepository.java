package com.ibmec.mall.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.ibmec.mall.model.Produto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends CosmosRepository<Produto, String> {
    List<Produto> findByCategoria(String categoria);
    List<Produto> findByAtivoTrue();
} 