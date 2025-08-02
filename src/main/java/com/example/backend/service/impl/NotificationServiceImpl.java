package com.example.backend.service.impl;

import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;


    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notify(User recipient, String message) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Override
    public void markAsRead(Notification notification) {
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
