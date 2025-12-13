package com.olimpo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.dto.AuthenticationDTO;
import com.olimpo.dto.ProfileUpdateDTO;
import com.olimpo.dto.RegisterDTO;
import com.olimpo.models.Account;
import com.olimpo.models.Enums.Role;
import com.olimpo.models.Idea;
import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import com.olimpo.service.CloudinaryService;
import com.olimpo.models.IdeaFile;
import org.springframework.core.io.ByteArrayResource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "CLOUDINARY_CLOUD_NAME=teste-cloud",
                "CLOUDINARY_API_KEY=123456",
                "CLOUDINARY_API_SECRET=abcdef",
                "api.security.token.secret=segredo-teste-muito-longo-para-funcionar-jwt"
})
@ActiveProfiles("test")
public class SystemFlowTest {

        @LocalServerPort
        private int port;

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private KeywordRepository keywordRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private JavaMailSender javaMailSender;

        @MockBean
        private CloudinaryService cloudinaryService;

        private String baseUrl;

        @BeforeEach
        void setUp() throws java.io.IOException {
                baseUrl = "http://localhost:" + port;
                userRepository.deleteAll();

                if (keywordRepository.findByName("Tecnologia").isEmpty()) {
                        keywordRepository.save(new Keyword("Tecnologia"));
                }

                MimeMessage mimeMessage = mock(MimeMessage.class);
                when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

                IdeaFile dummyFile = new IdeaFile();
                dummyFile.setFileUrl("http://cloudinary.com/dummy.jpg");
                when(cloudinaryService.uploadFile(any(), any())).thenReturn(dummyFile);
        }

        @Test
        void testFullUserLifecycle() throws Exception {
                assertTrue(port > 0, "Server should be running on a random port > 0");
                System.out.println("System Test running on port: " + port);

                RegisterDTO registerDTO = new RegisterDTO(
                                "systemtest@user.com",
                                "Password123!",
                                "System User",
                                "CPF",
                                "12345678900",
                                Role.ESTUDANTE,
                                "Test University",
                                "Computer Science",
                                "11999999999");

                ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/register",
                                registerDTO,
                                String.class);
                if (registerResponse.getStatusCode() != HttpStatus.OK) {
                        System.err.println("Registration failed: " + registerResponse.getBody());
                }
                assertEquals(HttpStatus.OK, registerResponse.getStatusCode(), "Registration should succeed");

                Account user = userRepository.findByEmail("systemtest@user.com").orElseThrow();
                user.setEmailVerified(true);
                userRepository.save(user);

                AuthenticationDTO loginDTO = new AuthenticationDTO("systemtest@user.com", "Password123!");
                ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                                baseUrl + "/auth/login",
                                loginDTO,
                                String.class);
                assertEquals(HttpStatus.OK, loginResponse.getStatusCode(), "Login should succeed");

                JsonNode root = objectMapper.readTree(loginResponse.getBody());
                String token = root.path("token").asText();
                assertNotNull(token, "Token should be present in login response");
                assertFalse(token.isEmpty(), "Token should not be empty");

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                headers.setContentType(MediaType.APPLICATION_JSON);

                com.olimpo.dto.IdeaRequestDTO ideaDTO = new com.olimpo.dto.IdeaRequestDTO(
                                "System Test Idea",
                                "Created during system test",
                                500,
                                java.util.List.of("Tecnologia"));

                HttpHeaders multipartHeaders = new HttpHeaders();
                multipartHeaders.setBearerAuth(token);
                multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                org.springframework.util.LinkedMultiValueMap<String, Object> createIdeaBody = new org.springframework.util.LinkedMultiValueMap<>();
                createIdeaBody.add("data", objectMapper.writeValueAsString(ideaDTO));

                ByteArrayResource fileResource = new ByteArrayResource("fake image content".getBytes()) {
                        @Override
                        public String getFilename() {
                                return "test-image.jpg";
                        }
                };
                createIdeaBody.add("file", fileResource);

                HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> createIdeaRequest = new HttpEntity<>(
                                createIdeaBody, multipartHeaders);

                ResponseEntity<String> createIdeaResponse = restTemplate.postForEntity(
                                baseUrl + "/api/ideas",
                                createIdeaRequest,
                                String.class);
                if (createIdeaResponse.getStatusCode() != HttpStatus.OK) {
                        System.err.println("Idea creation failed: " + createIdeaResponse.getStatusCode() + " "
                                        + createIdeaResponse.getBody());
                }
                assertEquals(HttpStatus.OK, createIdeaResponse.getStatusCode(), "Idea creation should succeed");

                JsonNode createdIdeaNode = objectMapper.readTree(createIdeaResponse.getBody());
                assertNotNull(createdIdeaNode);
                int createdIdeaId = createdIdeaNode.path("id").asInt();
                assertTrue(createdIdeaId > 0);
                assertEquals("System Test Idea", createdIdeaNode.path("name").asText());

                verify(cloudinaryService).uploadFile(any(), any());

                com.olimpo.dto.IdeaRequestDTO updateIdeaDTO = new com.olimpo.dto.IdeaRequestDTO(
                                "Updated System Test Idea",
                                "Created during system test",
                                600,
                                java.util.List.of("Tecnologia"));

                org.springframework.util.LinkedMultiValueMap<String, Object> updateIdeaBody = new org.springframework.util.LinkedMultiValueMap<>();
                updateIdeaBody.add("data", objectMapper.writeValueAsString(updateIdeaDTO));

                HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> updateIdeaRequest = new HttpEntity<>(
                                updateIdeaBody, multipartHeaders);

                ResponseEntity<String> updateIdeaResponse = restTemplate.exchange(
                                baseUrl + "/api/ideas/" + createdIdeaId,
                                HttpMethod.PUT,
                                updateIdeaRequest,
                                String.class);
                assertEquals(HttpStatus.OK, updateIdeaResponse.getStatusCode(), "Idea update should succeed");
                JsonNode updatedIdeaNode = objectMapper.readTree(updateIdeaResponse.getBody());
                assertEquals("Updated System Test Idea", updatedIdeaNode.path("name").asText());

                ProfileUpdateDTO profileUpdateDTO = new ProfileUpdateDTO(
                                "Updated System User",
                                "systemtest@user.com",
                                "SP",
                                "Test University",
                                "Software Engineering",
                                "Updated Bio",
                                "CPF",
                                "12345678900");

                org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
                body.add("data", objectMapper.writeValueAsString(profileUpdateDTO));

                HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> updateProfileRequest = new HttpEntity<>(
                                body, multipartHeaders);

                ResponseEntity<String> updateProfileResponse = restTemplate.exchange(
                                baseUrl + "/user/profile",
                                HttpMethod.PUT,
                                updateProfileRequest,
                                String.class);
                assertEquals(HttpStatus.OK, updateProfileResponse.getStatusCode(), "Profile update should succeed");

                ResponseEntity<String> getProfileResponse = restTemplate.exchange(
                                baseUrl + "/user/profile",
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                String.class);
                JsonNode profileNode = objectMapper.readTree(getProfileResponse.getBody());
                assertEquals("Updated System User", profileNode.path("name").asText());
                assertEquals("Software Engineering", profileNode.path("curso").asText());

                ResponseEntity<String> deleteResponse = restTemplate.exchange(
                                baseUrl + "/user/profile",
                                HttpMethod.DELETE,
                                new HttpEntity<>(headers),
                                String.class);
                assertEquals(HttpStatus.OK, deleteResponse.getStatusCode(), "Account deletion should succeed");
                assertEquals("Conta excluída com sucesso.", deleteResponse.getBody());

                assertTrue(userRepository.findByEmail("systemtest@user.com").isEmpty(),
                                "User should be deleted from database");
        }
}
