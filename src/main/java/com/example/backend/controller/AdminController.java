package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.model.User;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.service.LeaveRequestService;
import com.example.backend.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceImpl userService;
    private final LeaveRequestService leaveRequestService;
    private final LeaveRequestRepository leaveRequestRepository;



    // ========== EMPLOYEE MANAGEMENT ENDPOINTS ==========
    
    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<User> employees = userService.getAllEmployees();
        List<EmployeeDto> employeeDtos = employees.stream()
                .map(EmployeeDto::fromEntity)
                .toList();
        return ResponseEntity.ok(employeeDtos);
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable String id) {
        User employee = userService.getEmployeeById(id);
        EmployeeDto employeeDto = EmployeeDto.fromEntity(employee);
        return ResponseEntity.ok(employeeDto);
    }

    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createEmployee(@RequestBody CreateEmployeeRequest request) {
        User createdEmployee = userService.createEmployee(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Employee created successfully");
        response.put("employeeId", createdEmployee.getId());
        response.put("email", createdEmployee.getEmail());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateEmployee(@PathVariable String id, @RequestBody UpdateEmployeeRequest request) {
        User updatedEmployee = userService.updateEmployee(id, request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Employee updated successfully");
        response.put("employeeId", updatedEmployee.getId());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable String id) {
        userService.deleteUser(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Employee deleted successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/{id}/leave-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminLeaveRequestDto>> getEmployeeLeaveHistory(@PathVariable String id) {
        List<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveHistory(id);
        List<AdminLeaveRequestDto> dtos = requests.stream()
                .map(AdminLeaveRequestDto::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/employees/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetEmployeePassword(@PathVariable String id) {
        String newPassword = userService.resetEmployeePassword(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        response.put("newPassword", newPassword);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employees/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEmployeeStats() {
        Map<String, Object> stats = userService.getEmployeeStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/employees/on-leave")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesOnLeave(
            @RequestParam(value = "date", required = false) String dateStr
    ) {
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findEmployeesOnLeaveForDate(date);
        List<EmployeeDto> employeesOnLeave = leaveRequests.stream()
                .map(request -> EmployeeDto.fromEntity(request.getEmployee()))
                .distinct()
                .toList();
        
        return ResponseEntity.ok(employeesOnLeave);
    }

    // ========== LEGACY ENDPOINT (for backward compatibility) ==========
    
    @PostMapping("/create-employee")
    @PreAuthorize("hasRole('ADMIN')")
    @Deprecated
    public ResponseEntity<String> createEmployeeLegacy(@RequestBody CreateEmployeeRequest request) {
        userService.createEmployee(request);
        return ResponseEntity.ok("Employee created and email sent.");
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminLeaveRequestDto>> getAllRequests(
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "employeeId", required = false) String employeeId
    ) {
        LeaveStatus status = null;

        if (statusStr != null) {
            try {
                status = LeaveStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        List<LeaveRequest> requests = leaveRequestService.getAllRequests(status, employeeId);

        List<AdminLeaveRequestDto> dtos = requests.stream()
                .map(AdminLeaveRequestDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtos);
    }



    @PutMapping("/requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> approveRequest(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        leaveRequestService.updateStatus(id, LeaveStatus.ACCEPTED, reason);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Request approved successfully");
        response.put("status", "ACCEPTED");
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> rejectRequest(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reason is required for rejection");
            return ResponseEntity.badRequest().body(error);
        }
        
        leaveRequestService.updateStatus(id, LeaveStatus.REJECTED, reason);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Request rejected successfully");
        response.put("status", "REJECTED");
        response.put("reason", reason);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminLeaveRequestDto> getRequestById(@PathVariable UUID id) {
        LeaveRequest request = leaveRequestService.getRequestById(id);
        AdminLeaveRequestDto dto = AdminLeaveRequestDto.fromEntity(request);
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/requests/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getRequestStats() {
        List<LeaveRequest> allRequests = leaveRequestService.getAllRequests(null, null);
        
        long pending = allRequests.stream().filter(r -> r.getStatus() == LeaveStatus.PENDING).count();
        long approved = allRequests.stream().filter(r -> r.getStatus() == LeaveStatus.ACCEPTED).count();
        long rejected = allRequests.stream().filter(r -> r.getStatus() == LeaveStatus.REJECTED).count();
        long total = allRequests.size();
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("total", total);
        
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }


}
