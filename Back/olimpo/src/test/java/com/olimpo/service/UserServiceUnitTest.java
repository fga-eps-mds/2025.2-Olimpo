package com.olimpo.service;

import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
import com.olimpo.models.VerificationToken;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

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
    void cadastrarUsuario_EmailAlreadyExists_Throws() {
        RegisterDTO dto = new RegisterDTO("test@example.com", "pass", "Name", "CPF", "123", com.olimpo.models.Enums.Role.ESTUDANTE, null, null);
        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(new Account()));

        assertThrows(RuntimeException.class, () -> userService.cadastrarUsuario(dto));

        verify(userRepository).findByEmail(dto.email());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void cadastrarUsuario_Success_SavesAndSendsEmail() throws MessagingException {
        RegisterDTO dto = new RegisterDTO("new@example.com", "secret", "New", "CPF", "999", com.olimpo.models.Enums.Role.ESTUDANTE, "Fac", "Cur");

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-pass");
        when(userRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(42);
            return a;
        });

        var saved = userService.cadastrarUsuario(dto);

        assertNotNull(saved);
        assertEquals(42, saved.getId());
        assertEquals("encoded-pass", saved.getPassword());

        verify(userRepository).findByEmail(dto.email());
        verify(passwordEncoder).encode(dto.password());
        verify(userRepository).save(any(Account.class));
        verify(tokenRepository).deleteByUser(any(Account.class));
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmail(eq(dto.email()), anyString(), anyString());
    }

    @Test
    void verifyEmail_TokenNotFound_ReturnsFalse() {
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());
        assertFalse(userService.verifyEmail("abc"));
    }

    @Test
    void verifyEmail_TokenExpired_ReturnsFalse() {
        Account user = new Account();
        user.setId(1);
        VerificationToken token = new VerificationToken("t", user);
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("t")).thenReturn(Optional.of(token));
        assertFalse(userService.verifyEmail("t"));
    }

    @Test
    void verifyEmail_ValidToken_VerifiesAndDeletes() {
        Account user = new Account();
        user.setId(2);
        user.setEmailVerified(false);
        VerificationToken token = new VerificationToken("tok", user);

        when(tokenRepository.findByToken("tok")).thenReturn(Optional.of(token));

        assertTrue(userService.verifyEmail("tok"));

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void resendVerificationCode_UserNotFound_ReturnsFalse() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertFalse(userService.resendVerificationCode("x@x.com"));
    }

    @Test
    void resendVerificationCode_AlreadyVerified_ReturnsFalse() {
        Account user = new Account();
        user.setEmailVerified(true);
        when(userRepository.findByEmail("u@u.com")).thenReturn(Optional.of(user));
        assertFalse(userService.resendVerificationCode("u@u.com"));
    }

    @Test
    void resendVerificationCode_NotVerified_SendsEmail() throws MessagingException {
        Account user = new Account();
        user.setEmailVerified(false);
        user.setEmail("u@u.com");

        when(userRepository.findByEmail("u@u.com")).thenReturn(Optional.of(user));

        assertTrue(userService.resendVerificationCode("u@u.com"));

        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmail(eq("u@u.com"), anyString(), anyString());
    }
}
