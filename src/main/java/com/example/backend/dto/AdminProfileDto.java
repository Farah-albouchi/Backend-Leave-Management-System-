package com.example.backend.dto;

import com.example.backend.model.Role;
import com.example.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private Long cin;
    private LocalDate createdAt;
    private boolean profileCompleted;
    private Role role;

    public static AdminProfileDto fromEntity(User user) {
        return AdminProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .cin(user.getCin())
                .createdAt(user.getCreatedAt())
                .profileCompleted(user.isProfileCompleted())
                .role(user.getRole())
                .build();
    }
} 