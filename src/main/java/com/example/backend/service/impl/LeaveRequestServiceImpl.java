package com.example.backend.service.impl;

import com.example.backend.dto.DashboardSummary;
import com.example.backend.dto.LeaveRequestDto;
import com.example.backend.dto.MonthlyLeaveStat;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.LeaveRequestService;
import com.example.backend.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final NotificationService notificationService;


    @Override
    public LeaveRequest submitLeaveRequest(LeaveRequestDto dto, MultipartFile document, String userEmail) {
        User employee = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setHalfDay(dto.isHalfDay());
        request.setReason(dto.getReason());
        request.setType(dto.getType());
        request.setStatus(LeaveStatus.PENDING);
        request.setCreatedAt(LocalDate.now());

        if (document != null && !document.isEmpty()) {
            String path = fileStorageServiceImpl.saveFile(document);
            request.setDocumentPath(path);
        }
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.notify(admin, "New leave request submitted by " + employee.getFirstName() + " " + employee.getLastName());
        }

        return leaveRequestRepository.save(request);
    }

    @Override
    public List<LeaveRequest> getMyRequests(String userId) {
        return leaveRequestRepository.findByEmployeeId(userId);
    }

    @Override
    public LeaveRequest getMyRequestById(UUID requestId, String userId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getEmployee().getId().equals(userId.toString())) {
            throw new RuntimeException("You are not authorized to view this request.");
        }

        return request;
    }

    @Override
    public void cancelRequest(UUID requestId, String userId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!request.getEmployee().getId().equals(userId.toString())) {
            throw new RuntimeException("You are not authorized to cancel this request.");
        }

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be canceled.");
        }

        request.setStatus(LeaveStatus.CANCELED);
        leaveRequestRepository.save(request);
    }

    @Override
    public List<LeaveRequest> getAllRequests(LeaveStatus status, String employeeId) {
        if (status != null && employeeId != null) {
            return leaveRequestRepository.findByStatusAndEmployeeId(status, String.valueOf(employeeId));
        } else if (status != null) {
            return leaveRequestRepository.findByStatus(status);
        } else if (employeeId != null) {
            return leaveRequestRepository.findByEmployeeId(employeeId);
        } else {
            return leaveRequestRepository.findAll();
        }
    }

    @Override
    public LeaveRequest getRequestById(UUID requestId) {
        return leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    @Override
    public void updateStatus(UUID requestId, LeaveStatus status, String reason) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setStatus(status);
        request.setAdminRemark(status == LeaveStatus.REJECTED ? reason : null);

        leaveRequestRepository.save(request);
        String message = (status == LeaveStatus.ACCEPTED)
                ? "Your leave request has been approved."
                : "Your leave request has been rejected. Reason: " + reason;

        notificationService.notify(request.getEmployee(), message);
    }

    @Override
    public List<MonthlyLeaveStat> getMonthlyStats() {
        List<Object[]> results = leaveRequestRepository.countLeaveRequestsByMonth();

        return results.stream()
                .map(row -> {
                    String label = (String) row[0];     // "July 2025"
                    Long count = (Long) row[1];         // count
                    return new MonthlyLeaveStat(label.trim(), count); // trim removes extra padding
                })
                .collect(Collectors.toList());
    }


    @Override
    public DashboardSummary getDashboardSummary() {
        long total = leaveRequestRepository.count();
        long pending = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long approved = leaveRequestRepository.countByStatus(LeaveStatus.ACCEPTED);
        long rejected = leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);

        return new DashboardSummary(total, pending, approved, rejected);
    }
    @Override
    public Map<String, Long> getLeaveTypeDistribution() {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();

        return allRequests.stream()
                .collect(Collectors.groupingBy(LeaveRequest::getType, Collectors.counting()));
    }




}