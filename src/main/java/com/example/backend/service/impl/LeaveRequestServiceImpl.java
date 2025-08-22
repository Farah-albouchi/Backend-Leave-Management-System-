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
import java.time.temporal.ChronoUnit;
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
            String filename = fileStorageService.saveFile(document);
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
    public List<LeaveRequest> getEmployeeLeaveHistory(String employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    @Override
    public LeaveRequest getRequestById(UUID requestId) {
        return leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    @Override
    public void updateStatus(UUID requestId, LeaveStatus status, String reason) {
        LeaveRequest request = getRequestById(requestId);
        request.setStatus(status);
        
        if (reason != null && !reason.trim().isEmpty()) {
            request.setAdminRemark(reason);
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
        
        // Calculate total employees
        long totalEmployees = userRepository.count();
        
        // Calculate employees on leave today (simplified implementation)
        long employeesOnLeaveToday = 0; // TODO: Implement proper calculation
        
        // Calculate over-limit employees (employees with pending requests above normal)
        long overLimitEmployees = leaveRequestRepository.countByStatus(LeaveStatus.PENDING) > 5 ? 
            leaveRequestRepository.countByStatus(LeaveStatus.PENDING) - 5 : 0;

        return DashboardSummary.builder()
                .totalRequests(total)
                .pendingRequests(pending)
                .approvedRequests(approved)
                .rejectedRequests(rejected)
                .totalEmployees(totalEmployees)
                .employeesOnLeaveToday(employeesOnLeaveToday)
                .overLimitEmployees(overLimitEmployees)
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
    
    @Override
    public LeaveRequest saveLeaveRequest(LeaveRequest leaveRequest) {
        return leaveRequestRepository.save(leaveRequest);
    }

    @Override
    public Map<String, Long> getStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("APPROVED", leaveRequestRepository.countByStatus(LeaveStatus.ACCEPTED));
        distribution.put("PENDING", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        distribution.put("REJECTED", leaveRequestRepository.countByStatus(LeaveStatus.REJECTED));
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getTopEmployeesByDays(int limit) {
        // Simplified implementation
        List<LeaveRequest> approvedRequests = leaveRequestRepository.findByStatus(LeaveStatus.ACCEPTED);
        Map<String, Integer> employeeDays = new HashMap<>();
        Map<String, String> employeeNames = new HashMap<>();
        Map<String, Integer> employeeRequestCounts = new HashMap<>();
        
        for (LeaveRequest request : approvedRequests) {
            String employeeId = request.getEmployee().getId();
            String employeeName = request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName();
            int days = calculateWorkingDays(request.getStartDate(), request.getEndDate());
            
            employeeDays.put(employeeId, employeeDays.getOrDefault(employeeId, 0) + days);
            employeeNames.put(employeeId, employeeName);
            employeeRequestCounts.put(employeeId, employeeRequestCounts.getOrDefault(employeeId, 0) + 1);
        }
        
        return employeeDays.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> employee = new HashMap<>();
                    employee.put("employeeId", entry.getKey());
                    employee.put("employeeName", employeeNames.get(entry.getKey()));
                    employee.put("totalDays", entry.getValue());
                    employee.put("requests", employeeRequestCounts.get(entry.getKey()));
                    return employee;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRecentRequests(int limit) {
        List<LeaveRequest> requests = leaveRequestRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
                
        return requests.stream()
                .map(request -> {
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("id", request.getId());
                    requestData.put("employeeName", request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
                    requestData.put("leaveType", request.getType().toString());
                    requestData.put("startDate", request.getStartDate());
                    requestData.put("endDate", request.getEndDate());
                    requestData.put("duration", calculateWorkingDays(request.getStartDate(), request.getEndDate()));
                    requestData.put("status", request.getStatus().toString());
                    requestData.put("submittedAt", request.getCreatedAt());
                    requestData.put("reason", request.getReason());
                    return requestData;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getEmployeeBalances() {
        List<User> employees = userRepository.findByRole(Role.EMPLOYEE);
        return employees.stream()
                .map(employee -> {
                    Map<String, Object> balanceData = new HashMap<>();
                    balanceData.put("employeeId", employee.getId());
                    balanceData.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
                    
                    // Calculate balance - simplified logic
                    int totalAllowance = 25; // Default allowance
                    long usedDays = leaveRequestRepository.countByEmployeeAndStatus(employee, LeaveStatus.ACCEPTED);
                    int remaining = totalAllowance - (int) usedDays;
                    
                    balanceData.put("totalAllowance", totalAllowance);
                    balanceData.put("used", usedDays);
                    balanceData.put("remaining", remaining);
                    
                    return balanceData;
                })
                .collect(Collectors.toList());
    }

    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}