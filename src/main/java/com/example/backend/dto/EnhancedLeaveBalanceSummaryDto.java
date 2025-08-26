package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedLeaveBalanceSummaryDto {
    // Main KPI values
    private int totalPaidCap;           // Employee's total paid leave cap for the year
    private int totalUsedDays;          // Total days used across all paid types
    private int totalRemainingDays;     // Remaining paid days (cap - used)
    private int totalUnpaidDays;        // Total unpaid days taken
    private int totalPendingCount;      // Count of pending requests
    
    // Additional summary info
    private int currentYear;
    private boolean isUsingDefaultCap;  // Whether using system default or custom cap
    private int systemDefaultCap;       // For reference
    
    // Breakdown by leave type
    private List<EnhancedLeaveBalanceDto> balancesByType;
    
    // Upcoming approved leaves (next 30 days)
    private List<UpcomingLeaveDto> upcomingLeaves;
    
    // Usage percentage for progress bars
    private double usagePercentage;     // (used / cap) * 100
}


