package com.olimpo.service;

import com.olimpo.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.olimpo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public User create(String username, String password){
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmailVerified(false);

        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        user.setVerificationCode(code);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);
        try {
            String subject = "Confirmação de E-mail";
            String body = "Olá " +username + ",\n\n"
                    + "Seu código de verificação é: " + code + "\n"
                    + "Este código expira em 15 minutos.\n\n"
                    + "Atenciosamente,\n"
                    + "Equipe Lume.";
            emailService.sendEmail(username, subject, body);
        } catch (MessagingException | MailException e) {
            // Evita derrubar a requisição caso o envio de e-mail falhe (ex.: credenciais SMTP ausentes no Docker)
            e.printStackTrace();
        }

        return user;
    }

    public boolean verifyEmail(String username, String code) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;

        User user = optionalUser.get();
        if (user.getVerificationCode() == null
                || !user.getVerificationCode().equals(code)
                || user.getVerificationExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationExpiry(null);
        userRepository.save(user);
        return true;
    }

    public boolean resendVerificationCode(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;

        User user = optionalUser.get();
        if (user.isEmailVerified()) return false;

        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        user.setVerificationCode(code);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String subject = "Novo código de verificação";
        String body = "Seu novo código de verificação é: " + code;
        try {
            emailService.sendEmail(username, subject, body);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}