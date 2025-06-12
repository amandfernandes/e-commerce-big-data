package com.ibmec.mall.repository;

import com.ibmec.mall.model.Pedido;
import com.ibmec.mall.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByUsuario(Usuario usuario);
}
