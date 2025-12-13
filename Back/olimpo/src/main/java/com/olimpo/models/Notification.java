package com.olimpo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Account recipient; // Dono do post

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Account sender; // Quem curtiu

    @ManyToOne
    @JoinColumn(name = "idea_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Idea idea; // Qual post

    private String type; // "LIKE"

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
