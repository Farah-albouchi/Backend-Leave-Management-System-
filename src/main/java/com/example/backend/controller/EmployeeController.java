package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.impl.LeaveBalanceServiceImp;
import com.example.backend.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserServiceImpl userService;
    private final LeaveBalanceServiceImp leaveBalanceService;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestBody CompleteProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.completeProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Profile completed successfully.");
    }
    
    @GetMapping("/leave-balance")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveBalanceSummaryDto> getMyLeaveBalance(Principal principal) {
        String email = principal.getName();
        LeaveBalanceSummaryDto balanceSummary = leaveBalanceService.getEmployeeBalanceSummary(email);
        return ResponseEntity.ok(balanceSummary);
    }
    
    // ========== PROFILE ENDPOINTS ==========
    
    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ProfileDto> getMyProfile(Principal principal) {
        String email = principal.getName();
        ProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/me/profile")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ProfileDto> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Principal principal
    ) {
        String email = principal.getName();
        ProfileDto updatedProfile = userService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }
    
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<String> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal
    ) {
        String email = principal.getName();
        userService.changeAdminPassword(email, request); // This method works for all users
        return ResponseEntity.ok("Password changed successfully");
    }
}
