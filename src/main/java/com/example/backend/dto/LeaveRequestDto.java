package com.example.backend.dto;

import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LeaveRequestDto {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean halfDay;
    private String reason;
    private String type;
    private LeaveStatus status;
    private String documentPath;
    private String employeeName;

    public static LeaveRequestDto fromEntity(LeaveRequest request) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(request.getId());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setHalfDay(request.isHalfDay());
        dto.setReason(request.getReason());
        dto.setType(request.getType());
        dto.setStatus(request.getStatus());
        dto.setDocumentPath(request.getDocumentPath());
        dto.setEmployeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
        return dto;
    }
}
