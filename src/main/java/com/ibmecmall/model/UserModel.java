package com.ibmecmall.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "usuario")

public class UserModel {

    public Users() {
        this.cards = new ArrayList<>();
        this.address = new ArrayList<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    private LocalDate dtNascimento;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    private String telefone;
}
