package com.olimpo.controller;

import com.olimpo.models.Account;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.List;
import com.olimpo.dto.UserProfileDTO;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        boolean success = userService.verifyEmail(token);
        
        HttpHeaders headers = new HttpHeaders();
        
        if (success) {
            headers.setLocation(URI.create(frontendUrl + "/verificacao/sucesso"));
        } else {
            headers.setLocation(URI.create(frontendUrl + "/verificacao/falha"));
        }
        
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
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
    
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> searchByName(@RequestParam String name) {
        List<UserProfileDTO> results = userService.searchByName(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Integer id) {
        Optional<UserProfileDTO> dto = userService.getPublicProfile(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    
}