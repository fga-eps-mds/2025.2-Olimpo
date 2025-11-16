package com.olimpo;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.IdeaService;
import com.olimpo.repository.IdeaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.olimpo.service.EmailService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
                .orElseThrow(() -> new RuntimeException("Keyword 'Tecnologia' não populada pelo DataInitializer"));
        kwSaude = keywordRepository.findByName("Saúde")
                .orElseThrow(() -> new RuntimeException("Keyword 'Saúde' não populada pelo DataInitializer"));
    }

    @Test
    void testCreateIdea_Success_WithKeywords() {
        Idea newIdea = new Idea();
        newIdea.setName("Minha Grande Ideia de Tech");
        newIdea.setDescription("Uma descrição...");
        newIdea.setPrice(100);
        
        Set<Keyword> keywords = new HashSet<>();
        keywords.add(kwTech);
        newIdea.setKeywords(keywords);

        Idea savedIdea = ideaService.createIdea(newIdea, testAccount.getId());

        assertNotNull(savedIdea);
        assertNotNull(savedIdea.getId());
        assertEquals("Minha Grande Ideia de Tech", savedIdea.getName());
        assertNotNull(savedIdea.getTime());
        assertEquals(testAccount.getId(), savedIdea.getAccount().getId());

        assertNotNull(savedIdea.getKeywords());
        assertEquals(1, savedIdea.getKeywords().size());
        assertTrue(savedIdea.getKeywords().contains(kwTech));
    }

    @Test
    void testCreateIdea_Fail_InvalidAccountId() {
        Idea newIdea = new Idea();
        newIdea.setName("Ideia Fantasma");

        Integer invalidAccountId = -1;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ideaService.createIdea(newIdea, invalidAccountId);
        });

        assertEquals("Account não encontrado com id: " + invalidAccountId, exception.getMessage());
    }

    @Test
    void testUpdateIdea_Success_And_UpdateKeywords() {

        Idea originalIdea = new Idea();
        originalIdea.setName("Ideia Original");
        originalIdea.setAccount(testAccount);
        originalIdea.setKeywords(Set.of(kwTech));
        originalIdea = ideaRepository.save(originalIdea);
        Integer ideaId = originalIdea.getId();

        Idea ideaDetailsToUpdate = new Idea();
        ideaDetailsToUpdate.setName("Nome Atualizado");
        ideaDetailsToUpdate.setDescription("Descrição Nova");
        ideaDetailsToUpdate.setPrice(500);
        
        ideaDetailsToUpdate.setKeywords(Set.of(kwSaude));

        Idea updatedIdea = ideaService.updateIdea(ideaId, ideaDetailsToUpdate);

        assertNotNull(updatedIdea);
        assertEquals(ideaId, updatedIdea.getId());
        assertEquals("Nome Atualizado", updatedIdea.getName());
        assertEquals("Descrição Nova", updatedIdea.getDescription());
        assertEquals(500, updatedIdea.getPrice());
        assertEquals(testAccount.getId(), updatedIdea.getAccount().getId());

        assertNotNull(updatedIdea.getKeywords());
        assertEquals(1, updatedIdea.getKeywords().size());
        assertTrue(updatedIdea.getKeywords().contains(kwSaude));
        assertFalse(updatedIdea.getKeywords().contains(kwTech));
    }

    @Test
    void testDeleteIdea_Success() {
        Idea ideaToDelete = new Idea();
        ideaToDelete.setName("Ideia para deletar");
        ideaToDelete.setAccount(testAccount);
        ideaToDelete = ideaRepository.save(ideaToDelete);
        Integer ideaId = ideaToDelete.getId();

        assertTrue(ideaRepository.existsById(ideaId));

        ideaService.deleteIdea(ideaId);

        assertFalse(ideaRepository.existsById(ideaId));
    }
}