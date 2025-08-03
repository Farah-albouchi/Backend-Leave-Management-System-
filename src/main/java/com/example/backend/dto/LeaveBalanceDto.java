package com.example.backend.dto;

import com.example.backend.model.LeaveBalance;
import com.example.backend.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDto {
    private Long id;
    private LeaveType leaveType;
    private String leaveTypeName;
    private int totalAllowance;
    private int usedDays;
    private int remainingDays;
    private int pendingDays;
    private YearMonth period;
    
    public static LeaveBalanceDto fromEntity(LeaveBalance entity, int usedDays, int pendingDays) {
        return LeaveBalanceDto.builder()
                .id(entity.getId())
                .leaveType(entity.getLeaveType())
                .leaveTypeName(getLeaveTypeName(entity.getLeaveType()))
                .totalAllowance(entity.getRemainingDays() + usedDays)
                .usedDays(usedDays)
                .remainingDays(entity.getRemainingDays())
                .pendingDays(pendingDays)
                .period(entity.getPeriod())
                .build();
    }
    
    private static String getLeaveTypeName(LeaveType leaveType) {
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
}