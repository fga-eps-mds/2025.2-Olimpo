package com.olimpo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.config.SecurityConfig;
import com.olimpo.config.TestSecurityConfig;
import com.olimpo.dto.ProfileResponseDTO;
import com.olimpo.dto.ProfileUpdateDTO;
import com.olimpo.models.Account;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.AuthorizationService;
import com.olimpo.service.TokenService; // Import necessário
import com.olimpo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Optional;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, TestSecurityConfig.class, UserControllerTest.TestConfig.class })
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void verifyEmail_DeveRetornarFound_QuandoTokenValido() throws Exception {
        String validToken = "token-valido";

        when(userService.verifyEmail(validToken)).thenReturn(true);

        mockMvc.perform(get("/user/verify-email")
                .param("token", validToken))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5173/verificacao/sucesso"));
    }

    @Test
    void verifyEmail_DeveRetornarFound_QuandoTokenInvalido() throws Exception {
        String invalidToken = "token-invalido";

        when(userService.verifyEmail(invalidToken)).thenReturn(false);

        mockMvc.perform(get("/user/verify-email")
                .param("token", invalidToken))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:5173/verificacao/falha"));
    }

    @Test
    void resendCode_DeveRetornarOk_QuandoEmailValidoENaoVerificado() throws Exception {
        String email = "nao-verificado@email.com";

        Account userNaoVerificado = new Account();
        userNaoVerificado.setEmail(email);
        userNaoVerificado.setEmailVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userNaoVerificado));
        when(userService.resendVerificationCode(email)).thenReturn(true);

        mockMvc.perform(post("/user/resend-code")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Código reenviado com sucesso!"));
    }

    @Test
    void resendCode_DeveRetornarBadRequest_QuandoUsuarioNaoEncontrado() throws Exception {
        String email = "fantasma@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        mockMvc.perform(post("/user/resend-code")
                .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário não encontrado."));
    }

    @Test
    void resendCode_DeveRetornarBadRequest_QuandoUsuarioJaVerificado() throws Exception {
        String email = "verificado@email.com";

        Account userVerificado = new Account();
        userVerificado.setEmail(email);
        userVerificado.setEmailVerified(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userVerificado));

        mockMvc.perform(post("/user/resend-code")
                .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário já verificado."));
    }

    @Test
    void resendCode_DeveRetornarBadRequest_QuandoServicoFalha() throws Exception {
        String email = "nao-verificado@email.com";

        Account userNaoVerificado = new Account();
        userNaoVerificado.setEmail(email);
        userNaoVerificado.setEmailVerified(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userNaoVerificado));
        when(userService.resendVerificationCode(email)).thenReturn(false);

        mockMvc.perform(post("/user/resend-code")
                .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Falha ao reenviar o código."));
    }

    @Test
    void getProfile_DeveRetornarDadosDoUsuarioAutenticado() throws Exception {
        Account account = buildAccount();
        ProfileResponseDTO responseDTO = new ProfileResponseDTO(account);

        when(userService.getProfile(any(Account.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/user/profile")
                .with(authenticatedUser(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(account.getEmail()))
                .andExpect(jsonPath("$.name").value(account.getName()));
    }

    @Test
    void updateProfile_DeveRetornarPerfilAtualizado() throws Exception {
        Account account = buildAccount();

        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO(
                "Novo Nome",
                "novo@email.com",
                "DF",
                "UnB",
                "Engenharia",
                "Nova bio",
                "CPF",
                "12345678900");

        String payload = objectMapper.writeValueAsString(updateDTO);
        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                payload.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photoPart = new MockMultipartFile(
                "photo",
                "avatar.png",
                "image/png",
                "file".getBytes(StandardCharsets.UTF_8));

        Account updatedAccount = buildAccount();
        updatedAccount.setName("Novo Nome");
        updatedAccount.setEmail("novo@email.com");
        updatedAccount.setEstado("DF");
        updatedAccount.setFaculdade("UnB");
        updatedAccount.setCurso("Engenharia");
        updatedAccount.setBio("Nova bio");
        updatedAccount.setDocNumber("12345678900");

        ProfileResponseDTO responseDTO = new ProfileResponseDTO(updatedAccount);

        when(userService.updateProfile(any(Account.class), any(ProfileUpdateDTO.class), any())).thenReturn(responseDTO);

        mockMvc.perform(multipart("/user/profile")
                .file(dataPart)
                .file(photoPart)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(authenticatedUser(account)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    @Test
    void updateProfile_DeveRetornarBadRequest_QuandoDadosInvalidos() throws Exception {
        Account account = buildAccount();

        ProfileUpdateDTO updateDTO = new ProfileUpdateDTO(
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        String payload = objectMapper.writeValueAsString(updateDTO);
        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                payload.getBytes(StandardCharsets.UTF_8));

        when(userService.updateProfile(any(Account.class), any(ProfileUpdateDTO.class), any()))
                .thenThrow(new IllegalArgumentException("Nome não pode ser vazio"));

        mockMvc.perform(multipart("/user/profile")
                .file(dataPart)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(authenticatedUser(account)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nome não pode ser vazio"));
    }

    private Account buildAccount() {
        Account account = new Account();
        account.setId(1);
        account.setName("Usuário Teste");
        account.setEmail("teste@user.com");
        account.setPassword("senha");
        account.setRole("ESTUDANTE");
        account.setDocType("CPF");
        account.setDocNumber("00000000000");
        account.setEmailVerified(true);
        return account;
    }

    private @NonNull RequestPostProcessor authenticatedUser(Account account) {
        return java.util.Objects.requireNonNull(user(account));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        AuthorizationService authorizationService() {
            return Mockito.mock(AuthorizationService.class);
        }

    }
}