package com.example.backend.controller;

import com.example.backend.dto.DashboardSummary;
import com.example.backend.dto.MonthlyLeaveStat;
import com.example.backend.service.LeaveRequestService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AdminStatsController {

    private final LeaveRequestService leaveRequestService;
    private final UserService userService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveRequestService.getDashboardSummary());
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyLeaveStat>> getMonthlyTrend(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveRequestService.getMonthlyStats());
    }

    @GetMapping("/leave-types")
    public ResponseEntity<Map<String, Long>> getLeaveTypeDistribution(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leaveRequestService.getLeaveTypeDistribution());
    }

    @GetMapping("/status-distribution")
    public ResponseEntity<Map<String, Long>> getStatusDistribution(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String leaveType) {
        return ResponseEntity.ok(leaveRequestService.getStatusDistribution());
    }

    @GetMapping("/top-employees")
    public ResponseEntity<List<Map<String, Object>>> getTopEmployees(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String leaveType,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaveRequestService.getTopEmployeesByDays(limit));
    }

    @GetMapping("/recent-requests")
    public ResponseEntity<List<Map<String, Object>>> getRecentRequests(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(leaveRequestService.getRecentRequests(limit));
    }

    @GetMapping("/employee-balances")
    public ResponseEntity<List<Map<String, Object>>> getEmployeeBalances(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String balanceStatus) {
        return ResponseEntity.ok(leaveRequestService.getEmployeeBalances());
    }
}
