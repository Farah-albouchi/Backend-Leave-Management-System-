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
public class ProfileDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private Long cin;
    private Role role;
    private boolean profileCompleted;
    private LocalDate createdAt;
    private String fullName;

    public static ProfileDto fromEntity(User user) {
        return ProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .cin(user.getCin())
                .role(user.getRole())
                .profileCompleted(user.isProfileCompleted())
                .createdAt(user.getCreatedAt())
                .fullName(((user.getFirstName() != null ? user.getFirstName() : "") + 
                         " " + (user.getLastName() != null ? user.getLastName() : "")).trim())
                .build();
    }
}
