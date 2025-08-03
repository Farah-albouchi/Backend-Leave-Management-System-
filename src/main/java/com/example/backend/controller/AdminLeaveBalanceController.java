package com.example.backend.controller;

import com.example.backend.dto.LeaveBalanceSummaryDto;
import com.example.backend.service.impl.LeaveBalanceServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AdminLeaveBalanceController {

    private final LeaveBalanceServiceImp leaveBalanceService;

    @GetMapping("/employees/{employeeId}/leave-balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveBalanceSummaryDto> getEmployeeLeaveBalance(@PathVariable String employeeId) {
        LeaveBalanceSummaryDto balance = leaveBalanceService.getEmployeeLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }
}