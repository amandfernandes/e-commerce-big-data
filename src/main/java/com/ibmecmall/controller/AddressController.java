package com.ibmecmall.controller;

import com.ibmecmall.model.CardModel;
import com.ibmecmall.model.AddressModel;
import com.ibmecmall.model.UserModel;
import com.ibmecmall.repository.CardRepository;
import com.ibmecmall.repository.AddressRepository;
import com.ibmecmall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users/{id_user}/address")
public class AddressController {

    @Autowired
    private AddressRepository enderecoRepository;

    @Autowired
    private UserRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<UserModel> create(@PathVariable("id_user") int id_user, @RequestBody AddressModel address) {
        //Verificando se o usuario existe na base
        Optional<UserModel> optionalUsuario = this.usuarioRepository.findById(id_user);

        if (optionalUsuario.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //Cria o endereco  na base
        enderecoRepository.save(address);

        //Associa o endereco ao usuario
        UserModel user = optionalUsuario.get();

        user.getEnderecos().add(address);
        usuarioRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);

    }

}
