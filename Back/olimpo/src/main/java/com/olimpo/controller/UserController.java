package com.olimpo.controller;

import com.olimpo.models.Account;
import com.olimpo.dto.LoginRequest;
import com.olimpo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.web.bind.annotation.*;
import com.olimpo.service.UserService;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; 

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; 
    }
    
    @PostMapping
    public ResponseEntity<Account> createUser(@RequestBody Account usuario) {
        Account criarUsuario = userService.cadastrarUsuario(usuario);
        return ResponseEntity.ok(criarUsuario);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean success = userService.verifyEmail(token);
        if (success) {
            return ResponseEntity.ok("Email verificado com sucesso!"); 
        } else {
            return ResponseEntity.badRequest().body("Token inválido ou expirado.");
        }
    }
    
    @PostMapping("/resend-code")
    public ResponseEntity<String> resendCode(@RequestParam String email) {
        var opt = userRepository.findByEmail(email); 
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }
        if (opt.get().isEmailVerified()) {
            return ResponseEntity.badRequest().body("Usuário já verificado.");
        }
        boolean success = userService.resendVerificationCode(email);
        if (success) {
            return ResponseEntity.ok("Código reenviado com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Falha ao reenviar o código.");
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Account> optionalUser = userRepository.findByEmail(request.getEmail()); 
        
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
        
        Account user = optionalUser.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
        
        if (!user.isEmailVerified()) {
            return ResponseEntity.status(403).body("Email não verificado. Confirme seu email antes de fazer login.");
        }

        return ResponseEntity.ok("Login realizado com sucesso"); 
    }
}