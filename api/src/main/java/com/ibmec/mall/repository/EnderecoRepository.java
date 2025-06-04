package com.ibmec.mall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ibmec.mall.model.Endereco;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Integer> {
}
