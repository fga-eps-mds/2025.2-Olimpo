package com.olimpo.controller;

import com.olimpo.config.SecurityConfig;
import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.repository.IdeaFileRepository;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.AuthorizationService;
import com.olimpo.service.CloudinaryService;
import com.olimpo.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IdeaFileController.class)
@Import(SecurityConfig.class)
public class IdeaFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CloudinaryService cloudinaryService;
    @MockBean private IdeaRepository ideaRepository;
    @MockBean private IdeaFileRepository ideaFileRepository;

    @MockBean private TokenService tokenService;
    @MockBean private UserRepository userRepository;
    @MockBean private AuthorizationService authorizationService;

    @Test
    @WithMockUser(username = "usuario@teste.com")
    void uploadIdeaFiles_DeveSalvarArquivos_QuandoTudoCerto() throws Exception {
        
        Integer ideaId = 1;
        Idea ideaMock = new Idea();
        ideaMock.setId(ideaId);

        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(ideaMock));

        MockMultipartFile file1 = new MockMultipartFile("files", "img1.jpg", "image/jpeg", "bytes1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "img2.jpg", "image/jpeg", "bytes2".getBytes());

        IdeaFile uploadedFile = new IdeaFile(ideaMock, "img1.jpg", "image/jpeg", "http://url.fake/img1.jpg");
        when(cloudinaryService.uploadFile(any(), eq(ideaMock))).thenReturn(uploadedFile);

        mockMvc.perform(multipart("/api/ideas/{ideaId}/upload", ideaId)
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.message").value("Arquivos enviados com sucesso!"))

                .andExpect(jsonPath("$.fileUrls").isArray());
        
        verify(cloudinaryService, times(2)).uploadFile(any(), eq(ideaMock));
    }

    @Test
    @WithMockUser
    void deleteFile_DeveRemoverDoCloudinaryEDoBanco() throws Exception {
        
        Integer fileId = 50;
        IdeaFile fileMock = new IdeaFile();
        fileMock.setId(fileId);
        fileMock.setFileUrl("http://cloudinary.com/minha-foto.jpg");

        when(ideaFileRepository.findById(fileId)).thenReturn(Optional.of(fileMock));

        mockMvc.perform(delete("/api/ideas/files/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Arquivo deletado com sucesso"));

        verify(cloudinaryService).deleteFile(fileMock.getFileUrl());
        verify(ideaFileRepository).delete(fileMock);
        
    }
}