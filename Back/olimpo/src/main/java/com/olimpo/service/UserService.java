package com.olimpo.service;

import com.olimpo.dto.ProfileResponseDTO;
import com.olimpo.dto.ProfileUpdateDTO;
import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
import com.olimpo.models.VerificationToken;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailException;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import com.olimpo.dto.UserProfileDTO;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;
    private final CloudinaryService cloudinaryService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

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

    public ProfileResponseDTO getProfile(Account authenticatedUser) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Usuário não autenticado");
        }

        Integer userId = requireUserId(authenticatedUser);

        Account freshUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new ProfileResponseDTO(freshUser);
    }

    @Transactional
    public ProfileResponseDTO updateProfile(Account authenticatedUser, ProfileUpdateDTO data, MultipartFile photo) {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Usuário não autenticado");
        }
        if (data == null) {
            throw new IllegalArgumentException("Dados do perfil não enviados");
        }

        Integer userId = requireUserId(authenticatedUser);

        Account managedAccount = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        updateBasicInformation(managedAccount, data);
        updateDocumentInformation(managedAccount, data);

        boolean emailChanged = maybeUpdateEmail(managedAccount, data.email());

        if (photo != null && !photo.isEmpty()) {
            updateProfilePicture(managedAccount, photo);
        }

        Account saved = userRepository.save(Objects.requireNonNull(managedAccount));

        if (emailChanged) {
            sendVerificationEmail(saved);
        }

        return new ProfileResponseDTO(saved);
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
                a.getCurso(),
                a.getEstado(),
                a.getEmail());

    }

    private void updateBasicInformation(Account account, ProfileUpdateDTO data) {
        if (data.name() != null) {
            String sanitizedName = sanitizeMandatory(data.name(), "Nome não pode ser vazio");
            account.setName(sanitizedName);
        }

        if (data.estado() != null) {
            account.setEstado(sanitizeOptional(data.estado()));
        }

        if (data.faculdade() != null) {
            account.setFaculdade(sanitizeOptional(data.faculdade()));
        }

        if (data.curso() != null) {
            account.setCurso(sanitizeOptional(data.curso()));
        }

        if (data.bio() != null) {
            account.setBio(sanitizeOptional(data.bio()));
        }

    }

    private void updateDocumentInformation(Account account, ProfileUpdateDTO data) {
        if (data.docType() != null) {
            String sanitizedDocType = sanitizeMandatory(data.docType(), "Tipo de documento não pode ser vazio");
            account.setDocType(sanitizedDocType);
        }

        if (data.docNumber() != null) {
            String sanitizedDocNumber = sanitizeMandatory(data.docNumber(), "Número do documento não pode ser vazio");

            if (!sanitizedDocNumber.equals(account.getDocNumber()) &&
                    userRepository.existsByDocNumberAndIdNot(sanitizedDocNumber, account.getId())) {
                throw new IllegalArgumentException("Documento já está em uso por outro usuário");
            }

            account.setDocNumber(sanitizedDocNumber);
        }
    }

    private boolean maybeUpdateEmail(Account account, String newEmail) {
        if (newEmail == null) {
            return false;
        }

        String sanitizedEmail = sanitizeMandatory(newEmail, "E-mail não pode ser vazio");
        validateEmailFormat(sanitizedEmail);

        if (sanitizedEmail.equalsIgnoreCase(account.getEmail())) {
            return false;
        }

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(sanitizedEmail, account.getId())) {
            throw new IllegalArgumentException("E-mail já está em uso");
        }

        account.setEmail(sanitizedEmail);
        account.setEmailVerified(false);
        return true;
    }

    private void updateProfilePicture(Account account, MultipartFile photo) {
        try {
            if (account.getPfp() != null && !account.getPfp().isBlank()) {
                cloudinaryService.deleteFile(account.getPfp());
            }
            String pictureUrl = cloudinaryService.uploadProfilePicture(photo, account.getId());
            account.setPfp(pictureUrl);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao atualizar foto de perfil", e);
        }
    }

    private String sanitizeMandatory(String value, String errorMessage) {
        String sanitized = sanitizeOptional(value);
        if (sanitized == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return sanitized;
    }

    private String sanitizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("E-mail inválido");
        }
    }

    private @NonNull Integer requireUserId(Account account) {
        Integer id = account.getId();
        if (id == null) {
            throw new IllegalArgumentException("Usuário sem identificador válido");
        }
        return id;
    }
}