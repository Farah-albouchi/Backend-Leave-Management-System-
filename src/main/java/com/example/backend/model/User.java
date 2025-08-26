package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Builder
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private Long cin ;
    private LocalDate createdAt;
    @Column(nullable = false)
    @Builder.Default
    private boolean profileCompleted = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Maximum paid leave days for this employee (null means use system default)
    private Integer paidLeaveCapDays;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDate.now();
    }

    public Integer getPaidLeaveCapDays() {
        return paidLeaveCapDays;
    }

    public void setPaidLeaveCapDays(Integer capDays) {
        this.paidLeaveCapDays = capDays;
    }
}
