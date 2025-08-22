package com.example.backend.service;


import com.example.backend.dto.DashboardSummary;
import com.example.backend.dto.LeaveRequestDto;
import com.example.backend.dto.MonthlyLeaveStat;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface LeaveRequestService {
    LeaveRequest submitLeaveRequest(LeaveRequestDto dto, MultipartFile document, String userEmail);
    List<LeaveRequest> getMyRequests(String userId);
    LeaveRequest getMyRequestById(UUID requestId, String userId);
    void cancelRequest(UUID requestId,String userId);
    List<LeaveRequest> getAllRequests(LeaveStatus status, String employeeId);
    
    List<LeaveRequest> getEmployeeLeaveHistory(String employeeId);
    LeaveRequest getRequestById(UUID requestId);
    void updateStatus(UUID requestId, LeaveStatus status, String reason);
    List<MonthlyLeaveStat> getMonthlyStats();
    DashboardSummary getDashboardSummary();
    Map<String, Long> getLeaveTypeDistribution();
    
    // Admin methods
    LeaveRequest saveLeaveRequest(LeaveRequest leaveRequest);





}
