package com.olimpo.controller;

import com.olimpo.dto.LoginRequest;
import com.olimpo.models.User;
import com.olimpo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.olimpo.service.UserService;
import jakarta.mail.MessagingException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.create(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String username, @RequestParam String code) {
        boolean success = userService.verifyEmail(username, code);
        if (success) {
            return ResponseEntity.ok("Email verificado com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Código inválido ou expirado.");
        }
    }
    @PostMapping("/resend-code")
    public ResponseEntity<String> resendCode(@RequestParam String username) {
        boolean sucess = userService.resendVerificationCode(username);
        if(sucess){
            return ResponseEntity.ok("Código reenviado com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Usuário não encontrado ou já verificado.");
        }
    }
    @GetMapping("/confirm")
    public String getString(){
        return "confirm";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());
        
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
        
        User user = optionalUser.get();
        
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
        
        if (!user.isEmailVerified()) {
            return ResponseEntity.status(403).body("Email não verificado. Confirme seu email antes de fazer login.");
        }

        return ResponseEntity.ok("Login realizado com sucesso");
    }
}
