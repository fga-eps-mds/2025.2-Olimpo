package com.olimpo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.security.auth.Subject;

import com.olimpo.models.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", "minha-chave-secreta-teste");
    }

    @Test
    void testGenerateToken_deveGerarStringNaoVazia() {
        Account account = new Account();
        account.setEmail("usuario@gmail.com");
        String token = tokenService.generateToken(account);
        assertNotNull(token);
    }

    @Test
    void testValidateToken_deveRetornarVazioQunadoTokenInvalido() {
        String tokenInvalido = "token-invalido";
        String subject = tokenService.validateToken(tokenInvalido);
        assertEquals("", subject);
    }

    @Test
    void testValidateToken_deveRetornarEmailQuandoTokenValido() {

        Account account = new Account();
        account.setEmail("usuario@gmail.com");
        String token = tokenService.generateToken(account);
        String subject = tokenService.validateToken(token);
        assertEquals("usuario@gmail.com", subject);
    }

    
}
