package com.olimpo;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.IdeaService;
import com.olimpo.service.CloudinaryService;
import com.olimpo.repository.IdeaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.olimpo.service.EmailService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

@SpringBootTest(properties = {
    "CLOUDINARY_CLOUD_NAME=teste-cloud",
    "CLOUDINARY_API_KEY=123456",
    "CLOUDINARY_API_SECRET=abcdef",
    "api.security.token.secret=segredo-teste"
})
@Transactional
public class IdeaTests {

    @Autowired
    private IdeaService ideaService;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository accountRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private CloudinaryService cloudinaryService;

    private Account testAccount;
    private Keyword kwTech;
    private Keyword kwSaude;

    @BeforeEach
    void setUp() {

        ideaRepository.deleteAll();
        accountRepository.deleteAll();


        Account account = new Account();
        account.setName("Test User");
        account.setEmail("test@user.com");
        account.setPassword("123456");
        account.setRole("USER");
        account.setDocType("CPF");
        account.setDocNumber("12345678900");
        account.setEmailVerified(true);
        testAccount = accountRepository.save(account);

        kwTech = keywordRepository.findByName("Tecnologia")
                .orElseThrow(() -> new RuntimeException("Keyword 'Tecnologia' não encontrada no banco"));
        kwSaude = keywordRepository.findByName("Saúde")
                .orElseThrow(() -> new RuntimeException("Keyword 'Saúde' não encontrada no banco"));
    }

    @Test
    void testCreateIdea_Success_WithKeywords() {
        Idea newIdea = new Idea();
        newIdea.setName("Ideia Inovadora");
        newIdea.setDescription("Descrição teste");
        newIdea.setPrice(1000);
        
        newIdea.setKeywords(Set.of(kwTech));

        Idea savedIdea = ideaService.createIdea(newIdea, testAccount.getId());

        assertNotNull(savedIdea.getId());
        assertEquals("Ideia Inovadora", savedIdea.getName());
        assertEquals(testAccount.getId(), savedIdea.getAccount().getId());
        
        assertEquals(1, savedIdea.getKeywords().size());
        assertTrue(savedIdea.getKeywords().contains(kwTech));
    }

    @Test
    void testCreateIdea_Fail_InvalidAccountId() {
        Idea newIdea = new Idea();
        newIdea.setName("Ideia Fantasma");
        
        assertThrows(RuntimeException.class, () -> {
            ideaService.createIdea(newIdea, 99999);
        });
    }

    @Test
    void testUpdateIdea_UpdatesDetails_And_SwapsKeywords() {
        Idea idea = new Idea();
        idea.setName("Ideia Antiga");
        idea.setPrice(100);
        idea.setAccount(testAccount);
        idea.setKeywords(Set.of(kwTech));
        idea = ideaRepository.save(idea);

        Idea updateDetails = new Idea();
        updateDetails.setName("Ideia Nova");
        updateDetails.setDescription("Nova Descrição");
        updateDetails.setPrice(200);
        updateDetails.setKeywords(Set.of(kwSaude));

        Idea updatedIdea = ideaService.updateIdea(idea.getId(), updateDetails);

        assertEquals("Ideia Nova", updatedIdea.getName());
        assertEquals(200, updatedIdea.getPrice());
        
        assertTrue(updatedIdea.getKeywords().contains(kwSaude));
        assertFalse(updatedIdea.getKeywords().contains(kwTech));
    }

    @Test
    void testGetIdea_RetrievesAssociatedFiles() {
        Idea idea = new Idea();
        idea.setName("Ideia com Arquivos");
        idea.setAccount(testAccount);
        
        IdeaFile file1 = new IdeaFile(idea, "img1.jpg", "image/jpeg", "url1");
        idea.setIdeaFiles(List.of(file1));

        idea = ideaRepository.save(idea);

        Idea fetchedIdea = ideaService.getIdeaById(idea.getId());

        assertNotNull(fetchedIdea.getIdeaFiles());
        assertFalse(fetchedIdea.getIdeaFiles().isEmpty());
        assertEquals("url1", fetchedIdea.getIdeaFiles().get(0).getFileUrl());
    }

    @Test
    void testDeleteIdea_CascadesFiles_And_CallsCloudinary() throws IOException {
        Idea idea = new Idea();
        idea.setName("Ideia para Deletar");
        idea.setAccount(testAccount);
        
        IdeaFile file1 = new IdeaFile(idea, "f1.jpg", "image/jpeg", "url_cloudinary_1");
        IdeaFile file2 = new IdeaFile(idea, "f2.jpg", "image/jpeg", "url_cloudinary_2");
        
        List<IdeaFile> files = new ArrayList<>();
        files.add(file1);
        files.add(file2);
        idea.setIdeaFiles(files);

        idea = ideaRepository.save(idea);
        Integer ideaId = idea.getId();

        assertEquals(2, ideaRepository.findById(ideaId).get().getIdeaFiles().size());

        ideaService.deleteIdea(ideaId);

        assertFalse(ideaRepository.existsById(ideaId));
        
        verify(cloudinaryService, times(2)).deleteFile(anyString());
        
        verify(cloudinaryService).deleteFile("url_cloudinary_1");
        verify(cloudinaryService).deleteFile("url_cloudinary_2");
    }

    @Test
    void testDeleteIdea_WithoutFiles_DoesNotCallCloudinary() throws IOException {

        Idea idea = new Idea();
        idea.setName("Ideia Limpa");
        idea.setAccount(testAccount);
        idea = ideaRepository.save(idea);

        ideaService.deleteIdea(idea.getId());

        assertFalse(ideaRepository.existsById(idea.getId()));
        
        verify(cloudinaryService, never()).deleteFile(anyString());
    }
}