package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.VerificationToken;
import com.olimpo.repository.VerificationTokenRepository;
import com.olimpo.dto.RegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.olimpo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import com.olimpo.dto.UserProfileDTO;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public Account cadastrarUsuario(RegisterDTO data) {
        if (userRepository.findByEmail(data.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        Account usuario = new Account();
        usuario.setName(data.name());
        usuario.setEmail(data.email());
        usuario.setPassword(passwordEncoder.encode(data.password()));
        usuario.setDocType(data.docType());
        usuario.setDocNumber(data.docNumber());
        usuario.setRole(data.role().name());
        usuario.setEmailVerified(false);
        usuario.setFaculdade(data.faculdade());
        usuario.setCurso(data.curso());
        
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
            
            String verificationLink = "http://localhost:8080/user/verify-email?token=" + token;

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

    public List<UserProfileDTO> searchByName(String name) {
        var list = userRepository.findByNameContainingIgnoreCase(name == null ? "" : name);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<UserProfileDTO> getPublicProfile(Integer id) {
        return userRepository.findById(id).map(this::toDto);
    }

    private UserProfileDTO toDto(Account a) {
        return new UserProfileDTO(
                a.getId(),
                a.getName(),
                a.getPfp(),
                a.getBio(),
                a.getRole(),
                a.getFaculdade(),
                a.getSemestre(),
                a.getCurso(),
                a.getEstado()
        );
    }
}