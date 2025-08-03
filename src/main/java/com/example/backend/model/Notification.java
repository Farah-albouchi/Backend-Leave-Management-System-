package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String message;

    private String type; // success, info, warning, error

    private boolean read = false;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (type == null) {
            type = "info"; // Default type
        }
    }
}
