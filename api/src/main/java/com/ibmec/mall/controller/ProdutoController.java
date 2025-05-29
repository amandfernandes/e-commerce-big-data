package com.ibmec.mall.controller;

import com.ibmec.mall.model.Produto;
import com.ibmec.mall.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Produto> create(@RequestBody Produto produto) {
        produto.setId(UUID.randomUUID().toString()); // gera ID único
        repository.save(produto);
        return new ResponseEntity<>(produto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> getById(@PathVariable String id) {
        Optional<Produto> optProduto = repository.findById(id);

        if (optProduto.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(optProduto.get(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Produto>> getAll() {
        List<Produto> produtos = new ArrayList<>();
        repository.findAll().forEach(produtos::add);
        return new ResponseEntity<>(produtos, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<Produto> optProduto = repository.findById(id);

        if (optProduto.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        repository.delete(optProduto.get());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}