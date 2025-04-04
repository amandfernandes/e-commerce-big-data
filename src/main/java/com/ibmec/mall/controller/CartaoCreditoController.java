package com.ibmec.mall.controller;

import com.ibmec.mall.model.CartaoCredito;
import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.repository.CartaoCreditoRepository;
import com.ibmec.mall.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cartoes/{id_user}")
public class CartaoCreditoController {

    @Autowired
    private CartaoCreditoRepository cartaoCreditoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<CartaoCredito> create(@PathVariable("id_user") int id_user, @RequestBody CartaoCredito cartao) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id_user);

        if (optionalUsuario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // associa o usuário ao cartão
        cartao.setUsuario(optionalUsuario.get());

        cartaoCreditoRepository.save(cartao);

        return new ResponseEntity<>(cartao, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Iterable<CartaoCredito>> listByUser(@PathVariable("id_user") int id_user) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id_user);

        if (optionalUsuario.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Usuario usuario = optionalUsuario.get();
        return new ResponseEntity<>(usuario.getCartoes(), HttpStatus.OK);
    }
}