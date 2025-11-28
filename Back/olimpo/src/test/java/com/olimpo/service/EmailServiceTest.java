package com.olimpo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendPasswordResetEmail_ShouldSendSimpleMessageWithCorrectDetails() {
        String to = "user@example.com";
        String token = "reset-token-123";
        String baseUrl = "http://localhost:3000";

        emailService.sendPasswordResetEmail(to, token, baseUrl);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals("Recuperação de Senha - Olimpo", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(baseUrl + "/reset-password?token=" + token));
    }

    @Test
    void sendEmail_ShouldSendMimeMessage() throws MessagingException {
        String to = "user@example.com";
        String subject = "Bem-vindo";
        String body = "<h1>Olá!</h1>";

        MimeMessage mimeMessageMock = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessageMock);

        emailService.sendEmail(to, subject, body);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessageMock);
    }
}