package com.olimpo.controller;

import com.olimpo.config.SecurityConfig;
import com.olimpo.models.Account;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.AuthorizationService;
import com.olimpo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthorizationService authorizationService;

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
}