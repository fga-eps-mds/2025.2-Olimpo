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

// "O @WebMvcTest é pura economia de bateria: ele só liga o que precisa."
// "É tipo testar o motor do carro sem precisar ligar o ar-condicionado e o rádio junto."
@WebMvcTest(IdeaController.class)
@Import(SecurityConfig.class) // "Trazendo as regras de segurança pra portaria não barrar a gente à toa."
public class IdeaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // "Aqui a gente escala o elenco de apoio (Mocks). O Controller é a estrela, mas sem eles o filme não roda."
    @MockBean private IdeaService ideaService;
    @MockBean private CloudinaryService cloudinaryService;
    @MockBean private KeywordRepository keywordRepository;

    // "Os seguranças da balada (Security) pra deixarem a gente passar VIP."
    @MockBean private TokenService tokenService;
    @MockBean private UserRepository userRepository;
    @MockBean private AuthorizationService authorizationService;

    @Test
    // "Sem esse @WithMockUser, o Spring Security ia barrar a gente na porta."
    // "Com ele, a gente entra direto pro camarote."
    @WithMockUser(username = "criador@ideia.com", roles = "ESTUDANTE")
void createIdea_DeveRetornarOk_QuandoDadosValidos() throws Exception {
        
        // 1. Preparação da Autenticação (O conserto do erro 400)
        Account mockAccount = new Account();
        mockAccount.setId(1);
        mockAccount.setEmail("criador@ideia.com");
        mockAccount.setRole("ESTUDANTE");

        // Criamos a autenticação manualmente com o nosso objeto Account
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(mockAccount, null, mockAccount.getAuthorities());
        
        // Injetamos no contexto de segurança do teste
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Preparação dos Dados (igual ao anterior)
        IdeaRequestDTO requestDTO = new IdeaRequestDTO("Ideia Top", "Descricao", 500, List.of("Tech"));
        String jsonContent = objectMapper.writeValueAsString(requestDTO);

        MockMultipartFile jsonPart = new MockMultipartFile("data", "", "application/json", jsonContent.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "foto.jpg", "image/jpeg", "click".getBytes());

        Idea savedIdea = new Idea();
        savedIdea.setId(1);
        savedIdea.setName("Ideia Top");
        
        // Ajustamos o mock para aceitar qualquer ID de usuário (eq(1) ou any())
        when(ideaService.createIdea(any(Idea.class), eq(1))).thenReturn(savedIdea);

        // 3. Ação
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
        // "Esse é o teste mais 'paz e amor'. Só pede a lista e vê se ela chega."
        
        Idea ideia1 = new Idea();
        ideia1.setId(1);
        ideia1.setName("Ideia 1");
        
        when(ideaService.getAllIdeas()).thenReturn(List.of(ideia1));

        mockMvc.perform(get("/api/ideas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ideia 1"));
                
        // "Se a lista vier vazia ou cheia, o importante é o código 200 OK."
    }

    @Test
    @WithMockUser
    void updateIdea_DeveAtualizarComSucesso() throws Exception {
        // "Aqui tem um 'pulo do gato' técnico interessante."
        // "Por padrão, upload de arquivo é POST. Pra fazer um PUT com arquivo, a gente tem que forçar a barra um pouquinho."
        
        IdeaRequestDTO requestDTO = new IdeaRequestDTO("Ideia Atualizada", "Nova Desc", 999, null);
        String jsonContent = objectMapper.writeValueAsString(requestDTO);
        
        MockMultipartFile jsonPart = new MockMultipartFile("data", "", "application/json", jsonContent.getBytes());

        Idea updatedIdea = new Idea();
        updatedIdea.setId(1);
        updatedIdea.setName("Ideia Atualizada");

        when(ideaService.updateIdea(eq(1), any(), any())).thenReturn(updatedIdea);

        // "O truque: usamos o 'multipart', mas trocamos o método pra PUT na hora H."
        // "Isso mostra que a gente domina a ferramenta, não é só copiar e colar."
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/ideas/{id}", 1)
                        .file(jsonPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ideia Atualizada"));
    }

    @Test
    @WithMockUser
    void deleteIdea_DeveRetornarOk() throws Exception {
        // "Teste de faxina: curto e grosso."
        
        doNothing().when(ideaService).deleteIdea(1);

        mockMvc.perform(delete("/api/ideas/{id}", 1))
                .andExpect(status().isOk());
        
        // "Se não deu erro 500, quer dizer que limpou direitinho."
    }
}