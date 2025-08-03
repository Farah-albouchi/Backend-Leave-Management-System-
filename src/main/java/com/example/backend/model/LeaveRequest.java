package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employee;

    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status = LeaveStatus.PENDING;
    private boolean halfDay;

    @Column(name = "document_path")
    private String documentPath;

    private LocalDate createdAt;
    private String adminRemark;
    private LocalDate submittedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }
}
