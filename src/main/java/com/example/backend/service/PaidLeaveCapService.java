package com.example.backend.service;

import com.example.backend.model.User;

public interface PaidLeaveCapService {
    /**
     * Gets the effective paid leave cap for an employee (custom cap or system default)
     */
    int getEffectivePaidLeaveCap(User employee);
    
    /**
     * Calculates total paid leave days used by employee in current year
     */
    int getTotalPaidLeaveUsed(User employee, int year);
    
    /**
     * Calculates remaining paid leave quota for employee
     */
    int getRemainingPaidLeaveQuota(User employee, int year);
    
    /**
     * Checks if a leave request would exceed the paid leave cap and returns split info
     */
    com.example.backend.dto.PaidLeaveCapCheckResult checkPaidLeaveCapForRequest(User employee, int requestDays, int year);
    
    /**
     * Updates the paid leave cap for an employee
     */
    void updateEmployeePaidLeaveCap(String employeeId, Integer capDays);
    
    /**
     * Gets the system default paid leave cap
     */
    int getSystemDefaultPaidLeaveCap();
}
