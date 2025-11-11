package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.VerificationToken;
import com.olimpo.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.olimpo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;

// REMOVE import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public Account cadastrarUsuario(Account usuario) {
        if (userRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setEmailVerified(false);
        Account usuarioSalvo = userRepository.save(usuario);

        sendVerificationEmail(usuarioSalvo);

        return usuarioSalvo;
    }

    private void sendVerificationEmail(Account user) {

        tokenRepository.deleteByUser(user);
        
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        try {
            String subject = "Confirmação de E-mail - Olimpo";
            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            String body = "Olá " + user.getName() + ",\n\n"
                    + "Clique no link abaixo para verificar seu e-mail:\n"
                    + verificationLink + "\n\n"
                    + "Este link expira em 15 minutos.\n\n"
                    + "Atenciosamente,\n"
                    + "Equipe Olimpo.";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyEmail(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.isExpired()) {
            return false;
        }

        Account user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);
        return true;
    }

    public boolean resendVerificationCode(String email) {
        Optional<Account> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return false;
        }

        Account user = optionalUser.get();
        if (user.isEmailVerified()) {
            return false;
        }

        sendVerificationEmail(user);
        return true;
    }
}