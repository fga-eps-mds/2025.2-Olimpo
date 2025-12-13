package com.olimpo.repository;

import com.olimpo.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Integer recipientId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :recipientId")
    void deleteByRecipientId(Integer recipientId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.sender.id = :senderId")
    void deleteBySenderId(Integer senderId);
}
