package com.example.backend.service.impl;

import com.example.backend.dto.DashboardSummary;
import com.example.backend.dto.LeaveRequestDto;
import com.example.backend.dto.MonthlyLeaveStat;
import com.example.backend.model.*;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.FileStorageService;
import com.example.backend.service.LeaveRequestService;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public LeaveRequest submitLeaveRequest(LeaveRequestDto dto, MultipartFile document, String userEmail) {
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .type(dto.getType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .halfDay(dto.isHalfDay())
                .status(LeaveStatus.PENDING)
                .submittedAt(LocalDate.now())
                .build();

        if (document != null && !document.isEmpty()) {
            String filename = fileStorageService.storeFile(document);
            request.setDocumentPath(filename);
        }

        LeaveRequest saved = leaveRequestRepository.save(request);

        // Send notification to admins about new leave request
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        String message = String.format("New leave request from %s %s for %s to %s",
                employee.getFirstName(), employee.getLastName(),
                dto.getStartDate(), dto.getEndDate());

        for (User admin : admins) {
            notificationService.notify(admin, message, "info");
        }

        return saved;
    }

    @Override
    public List<LeaveRequest> getMyRequests(String userId) {
        return leaveRequestRepository.findByEmployeeId(userId);
    }

    @Override
    public LeaveRequest getMyRequestById(UUID requestId, String userId) {
        return leaveRequestRepository.findById(requestId)
                .filter(request -> request.getEmployee().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    @Override
    public void cancelRequest(UUID requestId, String userId) {
        LeaveRequest request = getMyRequestById(requestId, userId);
        if (request.getStatus() == LeaveStatus.PENDING) {
            request.setStatus(LeaveStatus.REJECTED);
            leaveRequestRepository.save(request);

            // Notify employee about cancellation
            String message = String.format("Your leave request for %s to %s has been cancelled",
                    request.getStartDate(), request.getEndDate());
            notificationService.notify(request.getEmployee(), message, "info");
        }
    }

    @Override
    public List<LeaveRequest> getAllRequests(LeaveStatus status, String employeeId) {
        if (status != null && employeeId != null) {
            return leaveRequestRepository.findByStatusAndEmployeeId(status, employeeId);
        } else if (status != null) {
            return leaveRequestRepository.findByStatus(status);
        } else if (employeeId != null) {
            return leaveRequestRepository.findByEmployeeId(employeeId);
        }
        return leaveRequestRepository.findAll();
    }

    @Override
    public LeaveRequest getRequestById(UUID requestId) {
        return leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    @Override
    public void updateStatus(UUID requestId, LeaveStatus status, String reason) {
        LeaveRequest request = getRequestById(requestId);
        LeaveStatus oldStatus = request.getStatus();
        request.setStatus(status);
        
        if (reason != null && !reason.trim().isEmpty()) {
            request.setAdminComment(reason);
        }
        
        leaveRequestRepository.save(request);

        // Send notification to employee about status change
        String statusText = status == LeaveStatus.ACCEPTED ? "approved" : "rejected";
        String notificationType = status == LeaveStatus.ACCEPTED ? "success" : "warning";
        
        String message = String.format("Your leave request for %s to %s has been %s",
                request.getStartDate(), request.getEndDate(), statusText);
        
        if (reason != null && !reason.trim().isEmpty()) {
            message += ". Reason: " + reason;
        }

        notificationService.notify(request.getEmployee(), message, notificationType);
    }

    @Override
    public List<MonthlyLeaveStat> getMonthlyStats() {
        List<Object[]> results = leaveRequestRepository.countLeaveRequestsByMonth();
        return results.stream()
                .map(result -> new MonthlyLeaveStat((String) result[0], (Long) result[1]))
                .collect(Collectors.toList());
    }

    @Override
    public DashboardSummary getDashboardSummary() {
        long total = leaveRequestRepository.count();
        long pending = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByStatus(LeaveStatus.ACCEPTED);
        long rejected = leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);

        return DashboardSummary.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .approvedRequests(approved)
                .rejectedRequests(rejected)
                .build();
    }

    @Override
    public Map<String, Long> getLeaveTypeDistribution() {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        return allRequests.stream()
                .collect(Collectors.groupingBy(
                        LeaveRequest::getType,
                        Collectors.counting()
                ));
    }
}