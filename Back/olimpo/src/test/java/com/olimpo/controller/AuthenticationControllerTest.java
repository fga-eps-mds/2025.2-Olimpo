package com.olimpo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.config.TestSecurityConfig;
import com.olimpo.dto.AuthenticationDTO;
import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
import com.olimpo.models.Enums.Role;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.AuthorizationService;
import com.olimpo.service.TokenService;
import com.olimpo.service.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class AuthenticationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private UserService userService;

        @MockBean
        private AuthorizationService authorizationService;

        @MockBean
        private TokenService tokenService;

        @MockBean
        private UserRepository userRepository;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                // Setup default behavior if needed, or remove if specific tests handle it
        }

        @Test
        void login_DeveRetornarOk_QuandoCredenciaisCorretas() throws Exception {
                AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "123456");

                Account userMock = new Account();
                userMock.setEmail("user@email.com");
                userMock.setRole("ESTUDANTE");

                UsernamePasswordAuthenticationToken authResult = new UsernamePasswordAuthenticationToken(userMock, null,
                                userMock.getAuthorities());

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(authResult);

                String tokenFalso = "token-jwt-mock-123";
                when(tokenService.generateToken(any(Account.class))).thenReturn(tokenFalso);

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(authDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value(tokenFalso));
        }

        @Test
        void login_DeveRetornarUnauthorized_QuandoCredenciaisInvalidas() throws Exception {
                AuthenticationDTO authDTO = new AuthenticationDTO("user@email.com", "senha-errada");
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

                ServletException exception = assertThrows(
                                ServletException.class,
                                () -> {
                                        mockMvc.perform(post("/auth/login")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(authDTO)));
                                });
                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertTrue(cause instanceof BadCredentialsException);
        }

        @Test
        void login_DeveRetornarForbidden_QuandoEmailNaoVerificado() throws Exception {
                AuthenticationDTO authDTO = new AuthenticationDTO("nao-verificado@email.com", "123456");
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new DisabledException("Usuário desabilitado"));

                ServletException exception = assertThrows(
                                ServletException.class,
                                () -> {
                                        mockMvc.perform(post("/auth/login")
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(objectMapper.writeValueAsString(authDTO)));
                                });
                Throwable cause = exception.getCause();
                assertNotNull(cause);
                assertTrue(cause instanceof DisabledException);
        }

        @Test
        void register_DeveRetornarOkEUsuario_QuandoDadosValidos() throws Exception {
                RegisterDTO registerDTO = new RegisterDTO("novo@email.com", "123456", "Novo User", "CPF", "111",
                                Role.INVESTIDOR, null, null, null);

                Account usuarioCriado = new Account();
                usuarioCriado.setId(10);
                usuarioCriado.setEmail("novo@email.com");
                usuarioCriado.setName("Novo User");

                when(userService.cadastrarUsuario(any(RegisterDTO.class))).thenReturn(usuarioCriado);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("novo@email.com"))
                                .andExpect(jsonPath("$.id").value(10));
        }

        @Test
        void register_DeveRetornarBadRequest_QuandoEmailJaExiste() throws Exception {
                RegisterDTO registerDTO = new RegisterDTO("existente@email.com", "123", "User", "CPF", "111",
                                Role.ESTUDANTE, null, null, null);

                when(userService.cadastrarUsuario(any(RegisterDTO.class)))
                                .thenThrow(new RuntimeException("E-mail já cadastrado"));

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDTO)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("E-mail já cadastrado"));
        }
}