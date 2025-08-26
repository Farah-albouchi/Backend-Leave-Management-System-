package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String message;
    private String type; // success, info, warning, error
    private boolean read;
    private LocalDateTime createdAt;
    private String recipientEmail;
    private String recipientName;
}
