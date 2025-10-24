package test.java.com.olimpo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

// --- IMPORTS DO CÓDIGO ---
import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.repository.AccountRepository;
import com.olimpo.repository.IdeaRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseHealthTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Test
    @Transactional
    void testDatabaseCrudAndRelationships() {
        
        // --- 1. Teste de CREATE (ACCOUNT) ---
        Account account = new Account();
        account.setName("Usuário de Teste");
        account.setEmail("teste@exemplo.com");
        account.setPassword("hash123");
        account.setRole("TESTER");
        account.setDocType("CPF");
        account.setDocNumber("12345678900");
        
        Account savedAccount = accountRepository.save(account);
        
        assertNotNull(savedAccount.getAccountId(), "O ID da conta não deveria ser nulo após salvar.");
        
        // --- 2. Teste de CREATE ---
        Idea idea = new Idea();
        idea.setName("Ideia de Teste");
        idea.setDescription("Descrição da ideia.");
        idea.setPrice(100);
        
        idea.setAccount(savedAccount); 
        
        Idea savedIdea = ideaRepository.save(idea);
        
        assertNotNull(savedIdea.getIdeaId(), "O ID da ideia não deveria ser nulo.");
        assertEquals(savedAccount.getAccountId(), savedIdea.getAccount().getAccountId(), "A ideia não foi associada corretamente à conta.");

        // --- 3. Teste de READ (UPDATE é similar) ---
        Optional<Account> foundAccount = accountRepository.findByEmail("teste@exemplo.com");
        
        assertTrue(foundAccount.isPresent(), "Não foi possível encontrar a conta por email.");
        assertEquals("Usuário de Teste", foundAccount.get().getName());

        // --- 4. Teste de DELETE ---
        
        accountRepository.delete(savedAccount);
        
        Optional<Account> deletedAccount = accountRepository.findByEmail("teste@exemplo.com");
        Optional<Idea> deletedIdea = ideaRepository.findById(savedIdea.getIdeaId());
        
        assertFalse(deletedAccount.isPresent(), "A conta não foi deletada.");
        assertFalse(deletedIdea.isPresent(), "A ideia não foi deletada em cascata (ON DELETE CASCADE falhou).");
        
        System.out.println(">>> SUCESSO! Teste de integração do banco concluído. CRUD e Relações OK. <<<");
    }
}