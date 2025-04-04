package com.ibmec.mall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ibmec.mall.model.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}
