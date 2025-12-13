package com.olimpo;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Notification;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.repository.NotificationRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationDeletionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.mail.javamail.JavaMailSender javaMailSender;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.olimpo.service.CloudinaryService cloudinaryService;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @Transactional
    public void testDeleteUserWithNotifications() {
        // Create User A (Recipient)
        Account userA = new Account();
        userA.setName("User A");
        userA.setEmail("usera@example.com");
        userA.setPassword("password");
        userA.setRole("ESTUDANTE");
        userA.setDocType("CPF");
        userA.setDocNumber("11111111111");
        userA = userRepository.save(userA);

        // Create User B (Sender)
        Account userB = new Account();
        userB.setName("User B");
        userB.setEmail("userb@example.com");
        userB.setPassword("password");
        userB.setRole("ESTUDANTE");
        userB.setDocType("CPF");
        userB.setDocNumber("22222222222");
        userB = userRepository.save(userB);

        // User A creates an Idea
        Idea idea = new Idea();
        idea.setName("Idea A");
        idea.setDescription("Description");
        idea.setPrice(100);
        idea.setAccount(userA);
        idea = ideaRepository.save(idea);

        // Create a Notification (User B liked User A's idea)
        Notification notification = new Notification();
        notification.setRecipient(userA);
        notification.setSender(userB);
        notification.setIdea(idea);
        notification.setType("LIKE");
        notificationRepository.save(notification);

        entityManager.flush();
        entityManager.clear();

        // Reload users to ensure they are managed
        Account finalUserB = userRepository.findById(userB.getId()).orElseThrow();
        Account finalUserA = userRepository.findById(userA.getId()).orElseThrow();

        // Try to delete User B (Sender)
        // This should delete the notification where User B is sender
        assertDoesNotThrow(() -> userService.deleteUser(finalUserB));
        assertTrue(userRepository.findById(finalUserB.getId()).isEmpty());

        // Create User C for second notification
        Account userC = new Account();
        userC.setName("User C");
        userC.setEmail("userc@example.com");
        userC.setPassword("password");
        userC.setRole("ESTUDANTE");
        userC.setDocType("CPF");
        userC.setDocNumber("33333333333");
        userC = userRepository.save(userC);

        // Reload idea as it might be detached
        Idea finalIdea = ideaRepository.findById(idea.getId()).orElseThrow();

        Notification notification2 = new Notification();
        notification2.setRecipient(finalUserA);
        notification2.setSender(userC);
        notification2.setIdea(finalIdea);
        notification2.setType("LIKE");
        notificationRepository.save(notification2);

        entityManager.flush();
        entityManager.clear();

        Account finalUserA2 = userRepository.findById(finalUserA.getId()).orElseThrow();

        // Now delete User A (Recipient)
        assertDoesNotThrow(() -> userService.deleteUser(finalUserA2));
        assertTrue(userRepository.findById(finalUserA2.getId()).isEmpty());
    }
}
