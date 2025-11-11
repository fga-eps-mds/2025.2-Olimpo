package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach; // Opcional, para setup
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Importante para capturar o objeto salvo
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*; // Para as verificações (asserts)
import static org.mockito.Mockito.*; // Para configurar e verificar mocks


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @Mock// Cria um mock para o UserRepository
    private UserRepository userRepository;

    @Mock // Cria um mock para o PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Cria uma instância real do UserService e injeta os @Mocks nele
    private UserService userService;

    @Test
    void cadastrarUsuario_DeveHashearSenhaESalvarUsuario_QuandoDadosValidos(){
        // Criando um objeto Account de entrada (com senha pura)
        Account usuario = new Account();
        usuario.setName("Teste Unitario");
        usuario.setPassword("123456");
        usuario.setEmail("teste@123.com");

        // Definindo o comportamento esperado dos mocks
        String senhaHasheadaEsperada = "$2a$10$algumHashSimulado";
        when(passwordEncoder.encode("123456")).thenReturn(senhaHasheadaEsperada);

        // Quando o save for chamado com QUALQUER Account, retorne o próprio objeto
        // (Simulando o save do banco que retorna o objeto salvo)

        when(userRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account accountSalvo = invocation.getArgument(0); // Pega o argumento passado para save
            accountSalvo.setId(1);
            return accountSalvo;
        });

        // 3. Crie um "captor" para verificar o objeto que foi passado para o save
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

        // Act (Execução)
        Account usuarioSalvo = userService.cadastrarUsuario(usuario);

        // Assert (Verificação)

        // 1. Verifique se o encode foi chamado com a senha pura
        verify(passwordEncoder).encode("123456");

        // 2. Verifique se o save foi chamado e capture o argumento
        verify(userRepository).save(accountCaptor.capture());
        Account usuarioPassadoParaSave = accountCaptor.getValue();

        // 3. Verifique se a senha no objeto PASSADO PARA O SAVE estava hasheada
        assertNotNull(usuarioPassadoParaSave);
        assertEquals(senhaHasheadaEsperada, usuarioPassadoParaSave.getPassword());
        assertEquals("Teste Unitario", usuarioPassadoParaSave.getName()); // Verifique outros campos se quiser

        // 4. Verifique se o objeto RETORNADO pelo método está correto
        assertNotNull(usuarioSalvo);
        assertEquals(1, usuarioSalvo.getId()); // Verifica o ID simulado
        assertEquals(senhaHasheadaEsperada, usuarioSalvo.getPassword());
    }
}
