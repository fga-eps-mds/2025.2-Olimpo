package com.olimpo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.dto.ProfileResponseDTO;
import com.olimpo.dto.ProfileUpdateDTO;
import com.olimpo.models.Account;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Optional;
import java.util.List;
import com.olimpo.dto.UserProfileDTO;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public UserController(UserService userService, UserRepository userRepository, ObjectMapper objectMapper) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(@AuthenticationPrincipal Account user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Account user,
            @RequestPart("data") String profilePayload,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ProfileUpdateDTO profileUpdateDTO = objectMapper.readValue(profilePayload, ProfileUpdateDTO.class);
            ProfileResponseDTO response = userService.updateProfile(user, profileUpdateDTO, photo);
            return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Não foi possível interpretar os dados enviados.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar o perfil. Tente novamente mais tarde.");
        }
    }

}