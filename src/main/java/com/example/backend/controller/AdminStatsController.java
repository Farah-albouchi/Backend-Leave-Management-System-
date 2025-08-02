package com.example.backend.controller;

import com.example.backend.dto.DashboardSummary;
import com.example.backend.dto.MonthlyLeaveStat;
import com.example.backend.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary() {
        return ResponseEntity.ok(leaveRequestService.getDashboardSummary());
    }


    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyLeaveStat>> getMonthlyTrend() {
        return ResponseEntity.ok(leaveRequestService.getMonthlyStats());
    }

    @GetMapping("/leave-types")
    public ResponseEntity<Map<String, Long>> getLeaveTypeDistribution() {
        return ResponseEntity.ok(leaveRequestService.getLeaveTypeDistribution());
    }



}
