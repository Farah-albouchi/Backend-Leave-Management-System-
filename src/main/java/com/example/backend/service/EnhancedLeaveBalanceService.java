package com.example.backend.service;

import com.example.backend.dto.EnhancedLeaveBalanceSummaryDto;
import com.example.backend.model.User;

public interface EnhancedLeaveBalanceService {
    /**
     * Get comprehensive leave balance summary for an employee
     */
    EnhancedLeaveBalanceSummaryDto getEnhancedLeaveBalanceSummary(String employeeEmail);
    
    /**
     * Get comprehensive leave balance summary for an employee by ID (admin use)
     */
    EnhancedLeaveBalanceSummaryDto getEnhancedLeaveBalanceSummaryById(String employeeId);
    
    /**
     * Refresh/recalculate leave balances for an employee
     */
    void refreshLeaveBalances(User employee);
}


