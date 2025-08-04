package com.example.backend.controller;

import com.example.backend.dto.CompleteProfileRequest;
import com.example.backend.dto.LeaveBalanceSummaryDto;
import com.example.backend.dto.UpdateEmployeeRequest;
import com.example.backend.dto.EmployeeDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import com.example.backend.service.impl.LeaveBalanceServiceImp;
import com.example.backend.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.security.Principal;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserServiceImpl userService;
    private final LeaveBalanceServiceImp leaveBalanceService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestBody CompleteProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.completeProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Profile completed successfully.");
    }
    
    @GetMapping("/leave-balance")
    public ResponseEntity<LeaveBalanceSummaryDto> getMyLeaveBalance(Principal principal) {
        String email = principal.getName();
        LeaveBalanceSummaryDto balanceSummary = leaveBalanceService.getEmployeeBalanceSummary(email);
        return ResponseEntity.ok(balanceSummary);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<EmployeeDto> getMyProfile(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        EmployeeDto employeeDto = EmployeeDto.fromEntity(user);
        return ResponseEntity.ok(employeeDto);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateMyProfile(
            @RequestBody UpdateEmployeeRequest request,
            Principal principal
    ) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update allowed fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCin() != null) {
            user.setCin(request.getCin());
        }
        
        // Mark profile as completed if all required fields are provided
        if (user.getFirstName() != null && user.getLastName() != null && 
            user.getPhone() != null && user.getAddress() != null && user.getCin() != null) {
            user.setProfileCompleted(true);
        }
        
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request,
            Principal principal
    ) {
        String email = principal.getName();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Current password and new password are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Current password is incorrect");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}
