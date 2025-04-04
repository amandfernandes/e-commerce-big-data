package com.ibmec.mall.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dtNascimento;

    private String cpf;
    private String telefone;

    @OneToMany(mappedBy = "usuario")
    private List<CartaoCredito> cartoes;

    @OneToMany(mappedBy = "usuario")
    private List<Endereco> enderecos;
}

