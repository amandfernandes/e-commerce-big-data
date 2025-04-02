package com.ibmecmall.controller;

import com.ibmecmall.model.UserModel;
import com.ibmecmall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository repository;

    @GetMapping
    public ResponseEntity<List<UserModel>> getUsers() {
        List<UserModel> response = repository.findAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserModel> getById(@PathVariable Integer id) {
        Optional<UserModel> response = this.repository.findById(id);
        if (response.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response.get() ,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserModel> create(@RequestBody UserModel user){
        this.repository.save(user);
        return new ResponseEntity<>(user ,HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<UserModel> delete(@PathVariable Integer id) {
        Optional<UserModel> response = this.repository.findById(id);
        if (response.isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //Exclui o usuario da base
        this.repository.delete(response.get());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}