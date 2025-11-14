package com.olimpo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token, String baseUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperação de Senha - Olimpo");
        message.setText("Para redefinir sua senha, clique no link abaixo:\n\n" +
                baseUrl + "/reset-password?token=" + token + "\n\n" +
                "Este link expira em 1 hora.\n\n" +
                "Se você não solicitou esta redefinição, ignore este e-mail.");
        mailSender.send(message);
    }

    public void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);
        mailSender.send(message);
    }
}