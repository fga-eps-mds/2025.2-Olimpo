package com.olimpo.service;

import com.olimpo.dto.ProfileResponseDTO;
import com.olimpo.dto.ProfileUpdateDTO;
import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private CloudinaryService cloudinaryService;

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
    void updateProfile_DeveAtualizarCamposBasicos_QuandoDadosValidos() {
        Account authenticated = createAccount();
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO(
                "Novo Nome",
                null,
                "DF",
                "UnB",
                5,
                "Engenharia",
                "Bio",
                null,
                null
        );

        when(userRepository.findById(authenticated.getId())).thenReturn(Optional.of(authenticated));
        when(userRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponseDTO responseDTO = userService.updateProfile(authenticated, updateDTO, null);

        assertEquals("Novo Nome", responseDTO.name());
        assertEquals("DF", responseDTO.estado());
        assertEquals("Engenharia", responseDTO.curso());
        assertEquals(5, responseDTO.semestre());
        verify(userRepository).save(authenticated);
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    void updateProfile_DeveLancarErro_QuandoEmailDuplicado() {
        Account authenticated = createAccount();
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO(
                null,
                "duplicado@email.com",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(userRepository.findById(authenticated.getId())).thenReturn(Optional.of(authenticated));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("duplicado@email.com", authenticated.getId())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(authenticated, updateDTO, null));

        assertEquals("E-mail já está em uso", exception.getMessage());
        verify(userRepository, never()).save(any(Account.class));
    }

    @Test
    void updateProfile_DeveAtualizarFotoDePerfil_QuandoArquivoInformado() throws IOException {
        Account authenticated = createAccount();
        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO(null, null, null, null, null, null, null, null, null);
        MockMultipartFile photo = new MockMultipartFile("photo", "pfp.png", "image/png", "img".getBytes());

        when(userRepository.findById(authenticated.getId())).thenReturn(Optional.of(authenticated));
        when(userRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cloudinaryService.uploadProfilePicture(photo, authenticated.getId())).thenReturn("https://cdn/pfp.png");

        ProfileResponseDTO responseDTO = userService.updateProfile(authenticated, updateDTO, photo);

        assertEquals("https://cdn/pfp.png", responseDTO.pfp());
        verify(cloudinaryService).uploadProfilePicture(photo, authenticated.getId());
        verify(userRepository).save(authenticated);
    }

    private Account createAccount() {
        Account account = new Account();
        account.setId(1);
        account.setName("Usuário");
        account.setEmail("original@email.com");
        account.setRole("ESTUDANTE");
        account.setDocType("CPF");
        account.setDocNumber("12345678900");
        account.setEmailVerified(true);
        return account;
    }
}