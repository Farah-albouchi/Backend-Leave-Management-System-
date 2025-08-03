package com.example.backend.service.impl;

import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notify(User recipient, String message) {
        notify(recipient, message, "info");
    }

    @Override
    public void notify(User recipient, String message, String type) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .type(type)
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

    @Override
    public void markAsRead(UUID notificationId, User user) {
        notificationRepository.findById(notificationId)
                .filter(notification -> notification.getRecipient().equals(user))
                .ifPresent(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Override
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        notifications.forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        });
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotification(UUID notificationId, User user) {
        notificationRepository.findById(notificationId)
                .filter(notification -> notification.getRecipient().equals(user))
                .ifPresent(notificationRepository::delete);
    }

    @Override
    public void clearAllNotifications(User user) {
        notificationRepository.deleteAllByRecipient(user);
    }

    @Override
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }
}
