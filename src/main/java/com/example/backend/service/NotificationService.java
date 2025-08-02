package com.example.backend.service;

import com.example.backend.model.Notification;
import com.example.backend.model.User;

import java.util.List;

public interface NotificationService {
    void notify(User recipient, String message);
    List<Notification> getUserNotifications(User user);
    void markAsRead(Notification notification);
}
