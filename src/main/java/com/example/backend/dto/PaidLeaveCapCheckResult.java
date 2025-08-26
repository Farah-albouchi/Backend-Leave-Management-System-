package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaidLeaveCapCheckResult {
    private int totalRequestDays;
    private int paidDays;
    private int unpaidDays;
    private int remainingQuota;
    private boolean exceedsQuota;
    private String message;
}


