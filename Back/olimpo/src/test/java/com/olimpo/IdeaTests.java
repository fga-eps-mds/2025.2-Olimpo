package com.olimpo;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
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

import java.util.Optional;

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

    @MockBean
    private EmailService emailService;

    private Account testAccount;

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
        testAccount = accountRepository.save(account);
    }

    // --- TESTE DE CRIAR (POSTAR) ---
    @Test
    void testCreateIdea_Success() {
        Idea newIdea = new Idea();
        newIdea.setName("Minha Grande Ideia");
        newIdea.setDescription("Uma descrição...");
        newIdea.setPrice(100);

        Idea savedIdea = ideaService.createIdea(newIdea, testAccount.getId());

        assertNotNull(savedIdea); // Não deve ser nulo
        assertNotNull(savedIdea.getId()); // Deve ter um ID do banco
        assertEquals("Minha Grande Ideia", savedIdea.getName()); // O nome deve bater
        assertNotNull(savedIdea.getTime()); // O @PrePersist deve ter funcionado
        assertEquals(testAccount.getId(), savedIdea.getAccount().getId()); // Deve estar ligado ao Account certo
    }

    @Test
    void testCreateIdea_Fail_InvalidAccountId() {
        Idea newIdea = new Idea();
        newIdea.setName("Ideia Fantasma");

        Integer invalidAccountId = 999999;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ideaService.createIdea(newIdea, invalidAccountId);
        });

        assertEquals("Account não encontrado com id: " + invalidAccountId, exception.getMessage());
    }

    @Test
    void testUpdateIdea_Success() {

        Idea originalIdea = new Idea();
        originalIdea.setName("Ideia Original");
        originalIdea.setAccount(testAccount);
        originalIdea = ideaRepository.save(originalIdea);
        Integer ideaId = originalIdea.getId();


        Idea ideaDetailsToUpdate = new Idea();
        ideaDetailsToUpdate.setName("Nome Atualizado");
        ideaDetailsToUpdate.setDescription("Descrição Nova");
        ideaDetailsToUpdate.setPrice(500);

        Idea updatedIdea = ideaService.updateIdea(ideaId, ideaDetailsToUpdate);

        assertNotNull(updatedIdea);
        assertEquals(ideaId, updatedIdea.getId()); // ID não deve mudar
        assertEquals("Nome Atualizado", updatedIdea.getName()); // Nome foi atualizado
        assertEquals("Descrição Nova", updatedIdea.getDescription()); // Descrição foi atualizada
        assertEquals(500, updatedIdea.getPrice()); // Preço foi atualizado
        assertEquals(testAccount.getId(), updatedIdea.getAccount().getId()); // Relação com Account se manteve
    }

    // --- TESTE DE DELETAR (REMOVER) ---
    @Test
    void testDeleteIdea_Success() {

        Idea ideaToDelete = new Idea();
        ideaToDelete.setName("Ideia para deletar");
        ideaToDelete.setAccount(testAccount);
        ideaToDelete = ideaRepository.save(ideaToDelete);
        Integer ideaId = ideaToDelete.getId();

        assertTrue(ideaRepository.existsById(ideaId));

        ideaService.deleteIdea(ideaId);

        // Procura no repositório. Não deve achar nada.
        assertFalse(ideaRepository.existsById(ideaId));
    }
}