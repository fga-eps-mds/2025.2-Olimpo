package com.olimpo.service;

import com.olimpo.models.PasswordResetToken;
import com.olimpo.models.Account;
import com.olimpo.repository.PasswordResetTokenRepository;
import com.olimpo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRY_HOURS = 1;

    public boolean requestPasswordReset(String email) {
        Optional<Account> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            Account user = userOptional.get();
            
            tokenRepository.deleteByUser(user);
            
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
            
            tokenRepository.save(resetToken);
            
            emailService.sendPasswordResetEmail(email, token, frontendUrl);
            
            return true;
        }
        return false;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        return resetToken.isPresent() && !resetToken.get().isExpired();
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOptional = tokenRepository.findByToken(token);
        
        if (resetTokenOptional.isPresent()) {
            PasswordResetToken resetToken = resetTokenOptional.get();
            
            if (resetToken.isExpired()) {
                return false;
            }
            
            Account user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            tokenRepository.delete(resetToken);
            
            return true;
        }
        return false;
    }
}