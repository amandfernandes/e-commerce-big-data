package com.ibmec.mall.controller;

import com.ibmec.mall.model.CartaoCredito;
import com.ibmec.mall.model.Usuario;
import com.ibmec.mall.repository.CartaoCreditoRepository;
import com.ibmec.mall.repository.UsuarioRepository;
import com.ibmec.mall.request.TransacaoRequest;
import com.ibmec.mall.request.TransacaoResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

     @PostMapping("/authorize")
    public ResponseEntity<TransacaoResponse> authorize(@PathVariable("id_user") int id_user,
                                                       @RequestBody TransacaoRequest request) {
        Optional<Usuario> optionalUsuario = this.usuarioRepository.findById(id_user);

        if (optionalUsuario.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (request.getNumero() == null || request.getCvv() == null || request.getValor() == null) {
            TransacaoResponse response = new TransacaoResponse();
            response.setStatus("NOT_AUTHORIZED");
            response.setData(LocalDateTime.now());
            response.setMensagem("Dados da transação incompletos");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Usuario user = optionalUsuario.get();
        CartaoCredito cartaoTransacao = null;

        for (CartaoCredito cartao: user.getCartoes()) {
            if (cartao.getNumero() != null && cartao.getCvv() != null &&
                cartao.getNumero().equals(request.getNumero()) && cartao.getCvv().equals(request.getCvv())) {
                cartaoTransacao = cartao;
                break;
            }
        }

        if (cartaoTransacao == null) {
            TransacaoResponse response = new TransacaoResponse();
            response.setStatus("NOT_AUTHORIZED");
            response.setData(LocalDateTime.now());
            response.setMensagem("Cartão não encontrado para o usuario");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        if (cartaoTransacao.getDtExpiracao() == null || cartaoTransacao.getDtExpiracao().isBefore(LocalDateTime.now())) {
            TransacaoResponse response = new TransacaoResponse();
            response.setStatus("NOT_AUTHORIZED");
            response.setData(LocalDateTime.now());
            response.setMensagem("Cartão Expirado");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (cartaoTransacao.getSaldo() == null || cartaoTransacao.getSaldo() < request.getValor()) {
            TransacaoResponse response = new TransacaoResponse();
            response.setStatus("NOT_AUTHORIZED");
            response.setData(LocalDateTime.now());
            response.setMensagem("Sem saldo para realizar a compra");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        Double saldo = cartaoTransacao.getSaldo() - request.getValor();
        cartaoTransacao.setSaldo(saldo);
        this.cartaoCreditoRepository.save(cartaoTransacao);

        TransacaoResponse response = new TransacaoResponse();
        response.setStatus("AUTHORIZED");
        response.setData(LocalDateTime.now());
        response.setMensagem("Compra autorizada");
        response.setCodigoAutorizacao(UUID.randomUUID().toString());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}