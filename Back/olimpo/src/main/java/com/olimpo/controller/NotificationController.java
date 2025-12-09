package com.olimpo.controller;

import com.olimpo.models.Account;
import com.olimpo.models.Notification;
import com.olimpo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal Account user) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());

        List<NotificationDTO> dtos = notifications.stream().map(n -> new NotificationDTO(
                n.getId(),
                n.getSender().getName(),
                n.getSender().getPfp(),
                n.getIdea().getName(),
                n.getIdea().getId(),
                n.getType(),
                n.getCreatedAt().toString())).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    public record NotificationDTO(Long id, String senderName, String senderAvatar, String ideaTitle, Integer ideaId,
            String type, String date) {
    }
}
