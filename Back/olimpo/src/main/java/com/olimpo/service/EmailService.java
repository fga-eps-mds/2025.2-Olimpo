package com.olimpo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
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
}