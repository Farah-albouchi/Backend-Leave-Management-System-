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
public class LeaveBalanceSummaryDto {
    private int totalAllowance;
    private int totalUsed;
    private int totalRemaining;
    private int totalPending;
    private List<LeaveBalanceDto> balancesByType;
    private int currentYear;
}