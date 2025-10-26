package com.olimpo.controller;

import com.olimpo.models.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.olimpo.service.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Account> createUser(@RequestBody Account usuario) {
        Account criarUsuario = userService.cadastrarUsuario(usuario);
        return ResponseEntity.ok(criarUsuario);
    }

    @GetMapping("/confirm")
    public String getString(){
        return "confirm";
    }
}
