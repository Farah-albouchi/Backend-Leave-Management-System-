package com.example.backend.dto;

import com.example.backend.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedLeaveBalanceDto {
    private LeaveType leaveType;
    private String leaveTypeName;
    private String leaveTypeDisplayName;  // User-friendly name
    
    // For traditional leave types (Vacation, Sick, etc.)
    private int allocatedDays;        // Total allocated for this type
    private int usedPaidDays;         // Used days that count toward paid quota
    private int usedUnpaidDays;       // Used days that are unpaid
    private int remainingDays;        // Remaining days for this type
    private int pendingCount;         // Number of pending requests for this type
    
    // Visual indicators
    private double usagePercentage;   // (used / allocated) * 100
    private String statusColor;       // For UI theming (green, yellow, red)
    private boolean isOverLimit;      // Whether usage exceeds allocation
    
    public static EnhancedLeaveBalanceDto create(
            LeaveType leaveType, 
            int allocated, 
            int usedPaid, 
            int usedUnpaid, 
            int pending) {
        
        String displayName = getDisplayName(leaveType);
        int remaining = Math.max(0, allocated - usedPaid);
        double usagePercentage = allocated > 0 ? ((double) usedPaid / allocated) * 100 : 0;
        String statusColor = getStatusColor(usagePercentage);
        boolean isOverLimit = usedPaid > allocated;
        
        return EnhancedLeaveBalanceDto.builder()
                .leaveType(leaveType)
                .leaveTypeName(leaveType.name())
                .leaveTypeDisplayName(displayName)
                .allocatedDays(allocated)
                .usedPaidDays(usedPaid)
                .usedUnpaidDays(usedUnpaid)
                .remainingDays(remaining)
                .pendingCount(pending)
                .usagePercentage(usagePercentage)
                .statusColor(statusColor)
                .isOverLimit(isOverLimit)
                .build();
    }
    
    private static String getDisplayName(LeaveType leaveType) {
        switch (leaveType) {
            case VACATION:
                return "Annual Leave";
            case SICK:
                return "Sick Leave";
            case MATERNITY:
                return "Maternity Leave";
            case UNPAID:
                return "Unpaid Leave";
            default:
                return leaveType.name();
        }
    }
    
    private static String getStatusColor(double usagePercentage) {
        if (usagePercentage >= 90) return "red";
        if (usagePercentage >= 70) return "yellow";
        return "green";
    }
}


