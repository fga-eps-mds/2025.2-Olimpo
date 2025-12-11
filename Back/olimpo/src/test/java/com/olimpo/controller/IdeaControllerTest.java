package com.olimpo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.config.SecurityConfig;
import com.olimpo.dto.IdeaRequestDTO;
import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.AuthorizationService;
import com.olimpo.service.CloudinaryService;
import com.olimpo.service.IdeaService;
import com.olimpo.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IdeaController.class)
@Import(SecurityConfig.class)
public class IdeaControllerTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private IdeaService ideaService;
        @MockBean
        private CloudinaryService cloudinaryService;
        @MockBean
        private KeywordRepository keywordRepository;
        @MockBean
        private com.olimpo.service.LikeService likeService;

        @MockBean
        private TokenService tokenService;
        @MockBean
        private UserRepository userRepository;
        @MockBean
        private AuthorizationService authorizationService;

        @Test
        @WithMockUser(username = "criador@ideia.com", roles = "ESTUDANTE")
        void createIdea_DeveRetornarOk_QuandoDadosValidos() throws Exception {

                Account mockAccount = new Account();
                mockAccount.setId(1);
                mockAccount.setEmail("criador@ideia.com");
                mockAccount.setRole("ESTUDANTE");

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                mockAccount, null,
                                mockAccount.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                IdeaRequestDTO requestDTO = new IdeaRequestDTO("Ideia Top", "Descricao", 500, List.of("Tech"));
                String jsonContent = objectMapper.writeValueAsString(requestDTO);

                MockMultipartFile jsonPart = new MockMultipartFile("data", "", "application/json",
                                jsonContent.getBytes());
                MockMultipartFile filePart = new MockMultipartFile("file", "foto.jpg", "image/jpeg",
                                "click".getBytes());

                Idea savedIdea = new Idea();
                savedIdea.setId(1);
                savedIdea.setName("Ideia Top");

                when(ideaService.createIdea(any(Idea.class), eq(1))).thenReturn(savedIdea);

                mockMvc.perform(multipart("/api/ideas")
                                .file(jsonPart)
                                .file(filePart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @WithMockUser
        void getAllIdeas_DeveRetornarLista() throws Exception {

                Idea ideia1 = new Idea();
                ideia1.setId(1);
                ideia1.setName("Ideia 1");

                when(ideaService.getAllIdeas()).thenReturn(List.of(ideia1));

                mockMvc.perform(get("/api/ideas"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].idea.name").value("Ideia 1"));
        }

        @Test
        @WithMockUser
        void updateIdea_DeveAtualizarComSucesso() throws Exception {

                IdeaRequestDTO requestDTO = new IdeaRequestDTO("Ideia Atualizada", "Nova Desc", 999, null);
                String jsonContent = objectMapper.writeValueAsString(requestDTO);

                MockMultipartFile jsonPart = new MockMultipartFile("data", "", "application/json",
                                jsonContent.getBytes());

                Idea updatedIdea = new Idea();
                updatedIdea.setId(1);
                updatedIdea.setName("Ideia Atualizada");

                when(ideaService.updateIdea(eq(1), any(), any())).thenReturn(updatedIdea);

                mockMvc.perform(multipart(HttpMethod.PUT, "/api/ideas/{id}", 1)
                                .file(jsonPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Ideia Atualizada"));
        }

        @Test
        @WithMockUser
        void deleteIdea_DeveRetornarOk() throws Exception {

                doNothing().when(ideaService).deleteIdea(1);

                mockMvc.perform(delete("/api/ideas/{id}", 1))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void toggleLike_DeveRetornarTrue_QuandoCurtiu() throws Exception {
                when(likeService.toggleLike(1, 1)).thenReturn(true);

                Account mockAccount = new Account();
                mockAccount.setId(1);
                mockAccount.setEmail("user@test.com");
                mockAccount.setRole("USER");
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAccount, null,
                                mockAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                mockMvc.perform(post("/api/ideas/{id}/like", 1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value(true));
        }

        @Test
        @WithMockUser
        void getLikes_DeveRetornarDTO() throws Exception {
                when(likeService.getLikeCount(1)).thenReturn(10L);
                when(likeService.isLikedByAccount(eq(1), any())).thenReturn(true);

                Account mockAccount = new Account();
                mockAccount.setId(1);
                mockAccount.setEmail("user@test.com");
                mockAccount.setRole("USER");
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAccount, null,
                                mockAccount.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                mockMvc.perform(get("/api/ideas/{id}/likes", 1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.count").value(10))
                                .andExpect(jsonPath("$.liked").value(true));
        }
}