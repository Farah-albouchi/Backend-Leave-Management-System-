package com.example.backend.repository;

import com.example.backend.model.Notification;
import com.example.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    @Transactional
    void deleteAllByRecipient(User user);

}
