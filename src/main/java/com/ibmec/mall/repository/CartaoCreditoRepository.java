package com.ibmec.mall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ibmec.mall.model.CartaoCredito;
import org.springframework.stereotype.Repository;

@Repository
public interface CartaoCreditoRepository extends JpaRepository<CartaoCredito, Integer> {
}

