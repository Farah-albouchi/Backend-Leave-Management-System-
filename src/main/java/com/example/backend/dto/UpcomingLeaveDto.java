package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingLeaveDto {
    private String id;
    private String leaveType;
    private String leaveTypeDisplayName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalDays;
    private boolean isHalfDay;
    private boolean isPaid;
    private String reason;
    
    // Computed fields
    private String dateRange;         // "Dec 25, 2024 - Dec 27, 2024"
    private String daysText;          // "3 days" or "0.5 day"
    private int daysUntilStart;       // Days from today until start date
    private String urgencyLevel;      // "immediate", "soon", "upcoming"
}


