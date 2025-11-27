package com.olimpo.service;

import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
import com.olimpo.models.VerificationToken;
import com.olimpo.models.Enums.Role;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void cadastrarUsuario_DeveHashearSenhaESalvarUsuario_QuandoDadosValidos() throws MessagingException {
    
        RegisterDTO registerDTO = new RegisterDTO(
                "teste@123.com",
                "123456",
                "Teste Unitario",
                "CPF",
                "12345678900",
                Role.ESTUDANTE,
                null,
                null
        );

        String senhaHasheadaEsperada = "$2a$10$algumHashSimulado";
        
        when(userRepository.findByEmail("teste@123.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn(senhaHasheadaEsperada);

        when(userRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account accountSalvo = invocation.getArgument(0);
            accountSalvo.setId(1);
            return accountSalvo;
        });
        
        doNothing().when(tokenRepository).deleteByUser(any(Account.class));
        when(tokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        Account usuarioSalvo = userService.cadastrarUsuario(registerDTO);

        verify(userRepository).findByEmail("teste@123.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(accountCaptor.capture());
        Account usuarioPassadoParaSave = accountCaptor.getValue();

        assertNotNull(usuarioPassadoParaSave);
        assertEquals(senhaHasheadaEsperada, usuarioPassadoParaSave.getPassword());
        assertEquals("Teste Unitario", usuarioPassadoParaSave.getName());
        assertEquals("teste@123.com", usuarioPassadoParaSave.getEmail());
        assertEquals("ESTUDANTE", usuarioPassadoParaSave.getRole());
        assertFalse(usuarioPassadoParaSave.isEmailVerified());
        assertNull(usuarioPassadoParaSave.getFaculdade());
        assertNull(usuarioPassadoParaSave.getCurso());

        verify(tokenRepository).deleteByUser(any(Account.class));
        verify(tokenRepository).save(any());
        verify(emailService).sendEmail(eq("teste@123.com"), anyString(), anyString());

        assertNotNull(usuarioSalvo);
        assertEquals(1, usuarioSalvo.getId());
        assertEquals(senhaHasheadaEsperada, usuarioSalvo.getPassword());
    }

    @Test
    void cadastrarUsuario_DeveLancarExcecao_QuandoEmailJaExiste() {
        
        RegisterDTO registerDTO = new RegisterDTO(
            "existente@123.com", "123", "User", "CPF", "111", Role.ESTUDANTE, null, null
        );

        when(userRepository.findByEmail("existente@123.com")).thenReturn(Optional.of(new Account()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.cadastrarUsuario(registerDTO);
        });

        assertEquals("E-mail já cadastrado", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(Account.class));
    }

    @Test
    void verifyEmail_DeveRetornarTrueEAtivarUsuario_QuandoTokenValido() {
        
        String tokenString = "token-valido-uuid";
        Account user = new Account();
        user.setEmail("teste@email.com");
        user.setEmailVerified(false);

        VerificationToken token = new VerificationToken();
        token.setToken(tokenString);
        token.setUser(user);
 
        token.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(15));

        when(tokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        boolean resultado = userService.verifyEmail(tokenString);

        assertTrue(resultado);
        assertTrue(user.isEmailVerified());
        
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void verifyEmail_DeveRetornarFalse_QuandoTokenNaoExiste() {

        String tokenInvalido = "token-inexistente";
        when(tokenRepository.findByToken(tokenInvalido)).thenReturn(Optional.empty());

        boolean resultado = userService.verifyEmail(tokenInvalido);

        assertFalse(resultado);
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void verifyEmail_DeveRetornarFalse_QuandoTokenExpirado() {
        String tokenExpiradoString = "token-expirado";
        VerificationToken token = new VerificationToken();
        token.setToken(tokenExpiradoString);
        token.setExpiryDate(java.time.LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken(tokenExpiradoString)).thenReturn(Optional.of(token));

        boolean resultado = userService.verifyEmail(tokenExpiradoString);

        assertFalse(resultado);
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }
}