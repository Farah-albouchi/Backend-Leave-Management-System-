package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.YearMonth;
@Data
@Entity
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User employee;

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    private int remainingDays;

    private YearMonth period;
}
