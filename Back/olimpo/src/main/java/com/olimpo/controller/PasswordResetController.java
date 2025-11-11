package com.olimpo.controller;

import com.olimpo.dto.PasswordResetDto;
import com.olimpo.dto.PasswordResetRequest;
import com.olimpo.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest request) {
        boolean success = passwordResetService.requestPasswordReset(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "Email de recuperação enviado com sucesso");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Email não encontrado");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDto resetDto) {
        if (!resetDto.getNewPassword().equals(resetDto.getConfirmPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "As senhas não coincidem");
            return ResponseEntity.badRequest().body(response);
        }

        if (resetDto.getNewPassword().length() < 6) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "A senha deve ter pelo menos 6 caracteres");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = passwordResetService.resetPassword(resetDto.getToken(), resetDto.getNewPassword());
        
        Map<String, String> response = new HashMap<>();
        if (success) {
            response.put("message", "Senha redefinida com sucesso");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Token inválido ou expirado");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateToken(token);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
}