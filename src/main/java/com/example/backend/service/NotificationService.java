package com.example.backend.service;

import com.example.backend.model.Notification;
import com.example.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void notify(User recipient, String message);
    void notify(User recipient, String message, String type);
    List<Notification> getUserNotifications(User user);
    void markAsRead(Notification notification);
    void markAsRead(UUID notificationId, User user);
    void markAllAsRead(User user);
    void deleteNotification(UUID notificationId, User user);
    void clearAllNotifications(User user);
    long getUnreadCount(User user);
}
