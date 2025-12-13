package com.olimpo;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Like;
import com.olimpo.models.Notification;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.repository.LikeRepository;
import com.olimpo.repository.NotificationRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.service.LikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "CLOUDINARY_CLOUD_NAME=teste-cloud",
        "CLOUDINARY_API_KEY=123456",
        "CLOUDINARY_API_SECRET=abcdef",
        "api.security.token.secret=segredo-teste"
})
@Transactional
public class LikeServiceTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.olimpo.service.EmailService emailService;

    private Account likerAccount;
    private Account ownerAccount;
    private Idea idea;

    @BeforeEach
    void setUp() {
        likeRepository.deleteAll();
        notificationRepository.deleteAll();
        ideaRepository.deleteAll();
        userRepository.deleteAll();

        likerAccount = new Account();
        likerAccount.setName("Liker User");
        likerAccount.setEmail("liker@user.com");
        likerAccount.setPassword("123456");
        likerAccount.setRole("USER");
        likerAccount.setDocType("CPF");
        likerAccount.setDocNumber("11111111111");
        likerAccount.setEmailVerified(true);
        likerAccount = userRepository.save(likerAccount);

        ownerAccount = new Account();
        ownerAccount.setName("Owner User");
        ownerAccount.setEmail("owner@user.com");
        ownerAccount.setPassword("123456");
        ownerAccount.setRole("USER");
        ownerAccount.setDocType("CPF");
        ownerAccount.setDocNumber("22222222222");
        ownerAccount.setEmailVerified(true);
        ownerAccount = userRepository.save(ownerAccount);

        idea = new Idea();
        idea.setName("Great Idea");
        idea.setDescription("Description");
        idea.setPrice(100);
        idea.setAccount(ownerAccount);
        idea = ideaRepository.save(idea);
    }

    @Test
    void testToggleLike_LikeAndUnlike() {
        boolean isLiked = likeService.toggleLike(idea.getId(), likerAccount.getId());
        assertTrue(isLiked);
        assertEquals(1, likeService.getLikeCount(idea.getId()));
        assertTrue(likeService.isLikedByAccount(idea.getId(), likerAccount.getId()));

        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(ownerAccount.getId());
        assertEquals(1, notifications.size());
        assertEquals("LIKE", notifications.get(0).getType());
        assertEquals(likerAccount.getId(), notifications.get(0).getSender().getId());

        isLiked = likeService.toggleLike(idea.getId(), likerAccount.getId());
        assertFalse(isLiked);
        assertEquals(0, likeService.getLikeCount(idea.getId()));
        assertFalse(likeService.isLikedByAccount(idea.getId(), likerAccount.getId()));
    }

    @Test
    void testToggleLike_SelfLike_DoesNotCreateNotification() {
        boolean isLiked = likeService.toggleLike(idea.getId(), ownerAccount.getId());
        assertTrue(isLiked);

        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(ownerAccount.getId());
        assertTrue(notifications.isEmpty());
    }
}
