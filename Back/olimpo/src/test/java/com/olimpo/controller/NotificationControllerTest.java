package com.olimpo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Notification;
import com.olimpo.repository.NotificationRepository;
import com.olimpo.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "CLOUDINARY_CLOUD_NAME=teste-cloud",
        "CLOUDINARY_API_KEY=123456",
        "CLOUDINARY_API_SECRET=abcdef",
        "api.security.token.secret=segredo-teste-muito-longo-para-funcionar-jwt"
})
@AutoConfigureMockMvc
@Transactional
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private com.olimpo.service.EmailService emailService;

    @Autowired
    private TokenService tokenService;

    private Account user;

    @BeforeEach
    void setUp() {
        user = new Account();
        user.setId(1);
        user.setName("Test User");
        user.setEmail("test@user.com");
        user.setRole("USER");
        user.setPfp("avatar.jpg");

        // Mock Authentication
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getNotifications_ShouldReturnList() throws Exception {
        Account sender = new Account();
        sender.setId(2);
        sender.setName("Sender User");
        sender.setPfp("sender_avatar.jpg");

        Idea idea = new Idea();
        idea.setId(10);
        idea.setName("Idea Title");

        Notification notification = new Notification();
        notification.setId(100L);
        notification.setRecipient(user);
        notification.setSender(sender);
        notification.setIdea(idea);
        notification.setType("LIKE");
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].senderName").value("Sender User"))
                .andExpect(jsonPath("$[0].ideaTitle").value("Idea Title"));
    }
}
