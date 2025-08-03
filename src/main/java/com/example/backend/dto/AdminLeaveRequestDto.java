package com.example.backend.dto;

import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
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
public class AdminLeaveRequestDto {
    private UUID id;
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean halfDay;
    private String reason;
    private String type;
    private LeaveStatus status;
    private String documentPath;
    private LocalDate submittedAt;
    private LocalDate createdAt;
    private String adminRemark;
    private int totalDays;

    public static AdminLeaveRequestDto fromEntity(LeaveRequest request) {
        AdminLeaveRequestDto dto = AdminLeaveRequestDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName())
                .employeeEmail(request.getEmployee().getEmail())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .halfDay(request.isHalfDay())
                .reason(request.getReason())
                .type(request.getType())
                .status(request.getStatus())
                .documentPath(request.getDocumentPath())
                .submittedAt(request.getSubmittedAt())
                .createdAt(request.getCreatedAt())
                .adminRemark(request.getAdminRemark())
                .totalDays(calculateWorkingDays(request.getStartDate(), request.getEndDate(), request.isHalfDay()))
                .build();
        return dto;
    }
    
    private static int calculateWorkingDays(LocalDate startDate, LocalDate endDate, boolean halfDay) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        
        int totalDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            // Skip weekends (Saturday = 6, Sunday = 7)
            if (current.getDayOfWeek().getValue() < 6) {
                totalDays++;
            }
            current = current.plusDays(1);
        }
        
        return halfDay && totalDays > 0 ? Math.max(1, totalDays) / 2 : totalDays;
    }
}