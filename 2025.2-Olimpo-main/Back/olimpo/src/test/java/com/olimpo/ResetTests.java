package com.olimpo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.controller.PasswordResetController;
import com.olimpo.dto.PasswordResetDto;
import com.olimpo.dto.PasswordResetRequest;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.PasswordResetService;
import com.olimpo.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PasswordResetController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordResetService passwordResetService;
    
    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PasswordResetRequest passwordResetRequest;
    private PasswordResetDto passwordResetDto;

    @BeforeEach
    void setUp() {
        passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setEmail("test@example.com");

        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setToken("valid-token");
        passwordResetDto.setNewPassword("password123");
        passwordResetDto.setConfirmPassword("password123");
    }

    @Test
    void shouldSendResetEmailSuccessfully() throws Exception {
        when(passwordResetService.requestPasswordReset("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/password/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email de recuperação enviado com sucesso"));
    }

    @Test
    void shouldFailWhenEmailNotFound() throws Exception {
        passwordResetRequest.setEmail("invalid@example.com");
        when(passwordResetService.requestPasswordReset("invalid@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/password/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email não encontrado"));
    }

    @Test
    void shouldFailWhenPasswordsDoNotMatch() throws Exception {
        passwordResetDto.setConfirmPassword("different");

        mockMvc.perform(post("/api/password/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("As senhas não coincidem"));
    }

    @Test
    void shouldFailWhenPasswordTooShort() throws Exception {
        passwordResetDto.setNewPassword("short");
        passwordResetDto.setConfirmPassword("short");

        mockMvc.perform(post("/api/password/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A senha deve ter pelo menos 6 caracteres"));
    }

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        when(passwordResetService.resetPassword("valid-token", "password123")).thenReturn(true);

        mockMvc.perform(post("/api/password/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso"));
    }

    @Test
    void shouldFailWhenTokenInvalid() throws Exception {
        passwordResetDto.setToken("invalid-token");
        when(passwordResetService.resetPassword("invalid-token", "password123")).thenReturn(false);

        mockMvc.perform(post("/api/password/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordResetDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token inválido ou expirado"));
    }

    @Test
    void shouldReturnValidToken() throws Exception {
        when(passwordResetService.validateToken("valid-token")).thenReturn(true);

        mockMvc.perform(get("/api/password/validate-token")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void shouldReturnInvalidToken() throws Exception {
        when(passwordResetService.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/password/validate-token")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}