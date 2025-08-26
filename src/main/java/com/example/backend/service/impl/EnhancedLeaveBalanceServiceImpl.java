package com.example.backend.service.impl;

import com.example.backend.dto.EnhancedLeaveBalanceDto;
import com.example.backend.dto.EnhancedLeaveBalanceSummaryDto;
import com.example.backend.dto.UpcomingLeaveDto;
import com.example.backend.model.*;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EnhancedLeaveBalanceService;
import com.example.backend.service.PaidLeaveCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedLeaveBalanceServiceImpl implements EnhancedLeaveBalanceService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired(required = false)
    private PaidLeaveCapService paidLeaveCapService;
    
    // Default allocations per leave type
    private static final Map<LeaveType, Integer> DEFAULT_ALLOCATIONS = Map.of(
            LeaveType.VACATION, 25,
            LeaveType.SICK, 10,
            LeaveType.MATERNITY, 90,
            LeaveType.UNPAID, 0  // Unlimited but tracked separately
    );
    
    @Override
    public EnhancedLeaveBalanceSummaryDto getEnhancedLeaveBalanceSummary(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return buildEnhancedSummary(employee);
    }
    
    @Override
    public EnhancedLeaveBalanceSummaryDto getEnhancedLeaveBalanceSummaryById(String employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return buildEnhancedSummary(employee);
    }
    
    @Override
    public void refreshLeaveBalances(User employee) {
        // Implementation for refreshing balances if needed
        // This could recalculate derived fields or update cached values
    }
    
    private EnhancedLeaveBalanceSummaryDto buildEnhancedSummary(User employee) {
        int currentYear = LocalDate.now().getYear();
        
        // Get paid leave cap information
        int totalPaidCap = 30; // Default
        boolean isUsingDefaultCap = true;
        int systemDefaultCap = 30;
        
        if (paidLeaveCapService != null) {
            totalPaidCap = paidLeaveCapService.getEffectivePaidLeaveCap(employee);
            systemDefaultCap = paidLeaveCapService.getSystemDefaultPaidLeaveCap();
            isUsingDefaultCap = employee.getPaidLeaveCapDays() == null;
        }
        
        // Get all requests for the current year
        List<LeaveRequest> allRequests = leaveRequestRepository
                .findByEmployeeAndStatusAndYear(employee, LeaveStatus.ACCEPTED, currentYear);
        List<LeaveRequest> pendingRequests = leaveRequestRepository
                .findByEmployeeAndStatusAndYear(employee, LeaveStatus.PENDING, currentYear);
        
        // Calculate totals
        int totalUsedDays = 0;
        int totalUnpaidDays = 0;
        Map<LeaveType, Integer> usedByType = new HashMap<>();
        Map<LeaveType, Integer> unpaidByType = new HashMap<>();
        Map<LeaveType, Integer> pendingByType = new HashMap<>();
        
        // Process approved requests
        for (LeaveRequest request : allRequests) {
            LeaveType type = getLeaveTypeFromString(request.getType());
            int days = calculateWorkingDays(request);
            
            usedByType.merge(type, days, Integer::sum);
            
            // Temporarily treat all requests as paid until database migration is complete
            boolean isPaid = true; // request.isPaid() when migration is done
            if (isPaid) {
                totalUsedDays += days;
            } else {
                totalUnpaidDays += days;
                unpaidByType.merge(type, days, Integer::sum);
            }
        }
        
        // Process pending requests
        for (LeaveRequest request : pendingRequests) {
            LeaveType type = getLeaveTypeFromString(request.getType());
            pendingByType.merge(type, 1, Integer::sum);
        }
        
        // Build per-type balances
        List<EnhancedLeaveBalanceDto> balancesByType = DEFAULT_ALLOCATIONS.entrySet().stream()
                .map(entry -> {
                    LeaveType type = entry.getKey();
                    int allocated = entry.getValue();
                    int usedPaid = usedByType.getOrDefault(type, 0) - unpaidByType.getOrDefault(type, 0);
                    int usedUnpaid = unpaidByType.getOrDefault(type, 0);
                    int pending = pendingByType.getOrDefault(type, 0);
                    
                    return EnhancedLeaveBalanceDto.create(type, allocated, usedPaid, usedUnpaid, pending);
                })
                .collect(Collectors.toList());
        
        // Get upcoming approved leaves
        List<UpcomingLeaveDto> upcomingLeaves = getUpcomingLeaves(employee);
        
        // Calculate remaining and usage percentage
        int totalRemainingDays = Math.max(0, totalPaidCap - totalUsedDays);
        double usagePercentage = totalPaidCap > 0 ? ((double) totalUsedDays / totalPaidCap) * 100 : 0;
        int totalPendingCount = pendingRequests.size();
        
        return EnhancedLeaveBalanceSummaryDto.builder()
                .totalPaidCap(totalPaidCap)
                .totalUsedDays(totalUsedDays)
                .totalRemainingDays(totalRemainingDays)
                .totalUnpaidDays(totalUnpaidDays)
                .totalPendingCount(totalPendingCount)
                .currentYear(currentYear)
                .isUsingDefaultCap(isUsingDefaultCap)
                .systemDefaultCap(systemDefaultCap)
                .balancesByType(balancesByType)
                .upcomingLeaves(upcomingLeaves)
                .usagePercentage(usagePercentage)
                .build();
    }
    
    private List<UpcomingLeaveDto> getUpcomingLeaves(User employee) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(30);
        
        List<LeaveRequest> upcomingRequests = leaveRequestRepository
                .findByEmployeeAndStatusAndYear(employee, LeaveStatus.ACCEPTED, today.getYear())
                .stream()
                .filter(request -> request.getStartDate().isAfter(today) && 
                                 request.getStartDate().isBefore(futureDate.plusDays(1)))
                .sorted(Comparator.comparing(LeaveRequest::getStartDate))
                .collect(Collectors.toList());
        
        return upcomingRequests.stream()
                .map(this::mapToUpcomingLeaveDto)
                .collect(Collectors.toList());
    }
    
    private UpcomingLeaveDto mapToUpcomingLeaveDto(LeaveRequest request) {
        LocalDate today = LocalDate.now();
        int daysUntilStart = (int) ChronoUnit.DAYS.between(today, request.getStartDate());
        String urgencyLevel = getUrgencyLevel(daysUntilStart);
        int totalDays = calculateWorkingDays(request);
        String daysText = request.isHalfDay() ? "0.5 day" : 
                         totalDays == 1 ? "1 day" : totalDays + " days";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String dateRange;
        if (request.getStartDate().equals(request.getEndDate())) {
            dateRange = request.getStartDate().format(formatter);
        } else {
            dateRange = request.getStartDate().format(formatter) + " - " + 
                       request.getEndDate().format(formatter);
        }
        
        return UpcomingLeaveDto.builder()
                .id(request.getId().toString())
                .leaveType(request.getType())
                .leaveTypeDisplayName(getDisplayName(request.getType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .isHalfDay(request.isHalfDay())
                .isPaid(true) // Temporarily hardcoded until migration is complete
                .reason(request.getReason())
                .dateRange(dateRange)
                .daysText(daysText)
                .daysUntilStart(daysUntilStart)
                .urgencyLevel(urgencyLevel)
                .build();
    }
    
    private String getUrgencyLevel(int daysUntilStart) {
        if (daysUntilStart <= 3) return "immediate";
        if (daysUntilStart <= 7) return "soon";
        return "upcoming";
    }
    
    private String getDisplayName(String leaveType) {
        switch (leaveType.toLowerCase()) {
            case "annual leave":
                return "Annual Leave";
            case "sick leave":
                return "Sick Leave";
            case "maternity leave":
                return "Maternity Leave";
            case "unpaid leave":
                return "Unpaid Leave";
            default:
                return leaveType;
        }
    }
    
    private LeaveType getLeaveTypeFromString(String typeString) {
        switch (typeString.toLowerCase()) {
            case "annual leave":
                return LeaveType.VACATION;
            case "sick leave":
                return LeaveType.SICK;
            case "maternity leave":
                return LeaveType.MATERNITY;
            case "unpaid leave":
                return LeaveType.UNPAID;
            default:
                return LeaveType.VACATION; // Default fallback
        }
    }
    
    private int calculateWorkingDays(LeaveRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        int totalDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            // Skip weekends (Saturday = 6, Sunday = 7)
            if (current.getDayOfWeek().getValue() < 6) {
                totalDays++;
            }
            current = current.plusDays(1);
        }
        
        if (request.isHalfDay() && totalDays > 0) {
            return Math.max(1, totalDays) / 2;
        }
        
        return totalDays;
    }
}
