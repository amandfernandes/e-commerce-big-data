package com.ibmec.mall.repository;

import com.ibmec.mall.model.Transacao;
import com.ibmec.mall.model.CartaoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {
    List<Transacao> findByCartao(CartaoCredito cartao);
}
