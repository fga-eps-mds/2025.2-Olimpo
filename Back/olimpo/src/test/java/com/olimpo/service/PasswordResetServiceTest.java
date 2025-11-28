package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.PasswordResetToken;
import com.olimpo.repository.PasswordResetTokenRepository;
import com.olimpo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void requestPasswordReset_DeveEnviarEmail_QuandoUsuarioExiste() {

        String email = "esqueceu@teste.com";
        Account user = new Account();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean result = passwordResetService.requestPasswordReset(email);

        assertTrue(result);

        verify(tokenRepository).deleteByUser(user);
        
        verify(tokenRepository).save(any(PasswordResetToken.class));
        
        verify(emailService).sendPasswordResetEmail(eq(email), anyString(), contains("http://localhost:3000"));
    }

    @Test
    void requestPasswordReset_DeveRetornarFalse_QuandoUsuarioNaoExiste() {
        String email = "fantasma@teste.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = passwordResetService.requestPasswordReset(email);

        assertFalse(result);
        
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void validateToken_DeveRetornarTrue_QuandoTokenValidoENaoExpirado() {
        String tokenString = "token-valido";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenString);
        token.setExpiryDate(LocalDateTime.now().plusHours(1)); 

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        assertTrue(passwordResetService.validateToken(tokenString));
    }

    @Test
    void validateToken_DeveRetornarFalse_QuandoTokenExpirado() {

        String tokenString = "token-vencido";
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenString);

        token.setExpiryDate(LocalDateTime.now().minusMinutes(1)); 

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        assertFalse(passwordResetService.validateToken(tokenString));
    }

    @Test
    void resetPassword_DeveTrocarSenha_QuandoTudoCerto() {

        String tokenString = "token-bom";
        String novaSenha = "senhaNova123";
        String hashNovaSenha = "$2a$10$hashDoidoSeguro";

        Account user = new Account();
        user.setPassword("senhaVelha");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenString);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        when(passwordEncoder.encode(novaSenha)).thenReturn(hashNovaSenha);

        boolean result = passwordResetService.resetPassword(tokenString, novaSenha);

        assertTrue(result);

        assertEquals(hashNovaSenha, user.getPassword());
        verify(userRepository).save(user);
        
        verify(tokenRepository).delete(token);
    }
}