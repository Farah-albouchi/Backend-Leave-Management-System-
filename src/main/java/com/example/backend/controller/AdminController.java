package com.example.backend.controller;

import com.example.backend.dto.CreateEmployeeRequest;
import com.example.backend.dto.LeaveRequestDto;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.service.LeaveRequestService;
import com.example.backend.service.UserService;
import com.example.backend.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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



    @PostMapping("/create-employee")
    @PreAuthorize("hasRole('ADMIN')") // ✅ only Admin
    public ResponseEntity<String> createEmployee(@RequestBody CreateEmployeeRequest request) {
        userService.createEmployee(request);
        return ResponseEntity.ok("Employee created and email sent.");
    }

    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequestDto>> getAllRequests(
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

        List<LeaveRequestDto> dtos = requests.stream()
                .map(LeaveRequestDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtos);
    }



    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable UUID id) {
        leaveRequestService.updateStatus(id, LeaveStatus.ACCEPTED, null);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        leaveRequestService.updateStatus(id, LeaveStatus.REJECTED, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<LeaveRequest> getRequestById(@PathVariable UUID id) {
        LeaveRequest request = leaveRequestService.getRequestById(id);
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }


}
