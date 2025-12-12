package com.olimpo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.dto.AuthenticationDTO;
import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
import com.olimpo.models.Enums.Role;
import com.olimpo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = {
                "CLOUDINARY_CLOUD_NAME=teste-cloud",
                "CLOUDINARY_API_KEY=123456",
                "CLOUDINARY_API_SECRET=abcdef",
                "api.security.token.secret=segredo-teste-muito-longo-para-funcionar-jwt"
})
@AutoConfigureMockMvc
@Transactional
public class AuthenticationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @org.springframework.boot.test.mock.mockito.MockBean
        private com.olimpo.service.EmailService emailService;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
        }

        @Test
        void testRegister_Success() throws Exception {
                RegisterDTO registerDTO = new RegisterDTO(
                                "new@user.com",
                                "123456",
                                "New User",
                                "CPF",
                                "12345678900",
                                Role.ESTUDANTE,
                                "Faculdade",
                                "Curso",
                                null);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("new@user.com"));
        }

        @Test
        void testLogin_Success() throws Exception {
                RegisterDTO registerDTO = new RegisterDTO(
                                "login@user.com",
                                "123456",
                                "Login User",
                                "CPF",
                                "12345678900",
                                Role.ESTUDANTE,
                                "Faculdade",
                                "Curso",
                                null);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDTO)))
                                .andExpect(status().isOk());

                Account user = userRepository.findByEmail("login@user.com").orElseThrow();
                user.setEmailVerified(true);
                userRepository.save(user);

                AuthenticationDTO loginDTO = new AuthenticationDTO("login@user.com", "123456");

                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDTO)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists());
        }
}
