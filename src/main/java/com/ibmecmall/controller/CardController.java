package com.ibmecmall.controller;

import com.ibmecmall.model.CardModel;
import com.ibmecmall.model.UserModel;
import com.ibmecmall.repository.CardRepository;
import com.ibmecmall.repository.UserRepository;
import com.ibmecmall.requests.TransacaoRequest;
import com.ibmecmall.requests.TransacaoResposta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("users/{id_user}/card")
public class CardController {

    @Autowired
    private CardRepository cartaoRepository;

    @Autowired
    private UserRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<UserModel> create(@PathVariable("id_user") int id_user, @RequestBody CardModel card) {
        //Verificando se o usuario existe na base
        Optional<UserModel> optionalUsuario = this.usuarioRepository.findById(id_user);

        if (optionalUsuario.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //Cria o cartao de credito na base
        cartaoRepository.save(card);

        //Associa o cartao de credito ao usuario
        UserModel user = optionalUsuario.get();

        user.getCard().add(card);
        usuarioRepository.save(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);

    }

    @PostMapping("/authorize")
    public ResponseEntity<TransacaoResposta> authorize(@PathVariable("id_user") int id_user,
                                                       @RequestBody TransacaoRequest request) {
        //Verificando se o usuario existe na base
        Optional<UserModel> optionalUsuario = this.usuarioRepository.findById(id_user);

        if (optionalUsuario.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        UserModel user = optionalUsuario.get();
        CardModel cartaoTransacao = null;

        for (CardModel cartao : user.getCard()) {
            if (cartao.getNumero().equals(request.getNumero()) && cartao.getCvv().equals(request.getCvv())) {
                cartaoTransacao = cartao;
                break;
            }
        }

        //Não achei o cartao associado para o usuario
        if (cartaoTransacao == null) {
            TransacaoResposta response = new TransacaoResposta();
            response.setStatus("NOT_AUTHORIZED");
            response.setDtTransacao(LocalDateTime.now());
            response.setMessage("Cartão não encontrado para o usuario");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Verifica se o cartao não está expirado
        if (cartaoTransacao.getDtExpiracao().isBefore(LocalDateTime.now())) {
            TransacaoResposta response = new TransacaoResposta();
            response.setStatus("NOT_AUTHORIZED");
            response.setDtTransacao(LocalDateTime.now());
            response.setMessage("Cartão Expirado");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //Verifica se tem dinheiro no cartao para realizr a compra
        if (cartaoTransacao.getSaldo() < request.getValor()) {
            TransacaoResposta response = new TransacaoResposta();
            response.setStatus("NOT_AUTHORIZED");
            response.setDtTransacao(LocalDateTime.now());
            response.setMessage("Sem saldo para realizar a compra");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        //Pega o saldo do cartão
        Double saldo = cartaoTransacao.getSaldo();

        //Substrai o saldo com o valor da compra
        saldo = saldo - request.getValor();

        //Atualiza o saldo do cartao
        cartaoTransacao.setSaldo(saldo);

        //Grava o novo saldo na base de dados
        this.cartaoRepository.save(cartaoTransacao);

        //Compra Autorizada
        TransacaoResposta response = new TransacaoResposta();
        response.setStatus("AUTHORIZED");
        response.setDtTransacao(LocalDateTime.now());
        response.setMessage("Compra autorizada");
        response.setCodigoAutorizacao(UUID.randomUUID());

        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}
