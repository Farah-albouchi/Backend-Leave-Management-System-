package com.example.backend.service.impl;

import com.example.backend.dto.LeaveBalanceDto;
import com.example.backend.dto.LeaveBalanceSummaryDto;
import com.example.backend.model.LeaveBalance;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.model.LeaveType;
import com.example.backend.model.User;
import com.example.backend.repository.LeaveBalanceRepository;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class LeaveBalanceServiceImp {

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepo;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;

    private static final Map<LeaveType, Integer> DEFAULT_BALANCES = Map.of(
            LeaveType.VACATION, 25,
            LeaveType.SICK, 10,
            LeaveType.MATERNITY, 90,
            LeaveType.UNPAID, 30
    );

    public List<LeaveBalance> getBalancesByEmployee(User employee) {
        return leaveBalanceRepo.findByEmployee_Id(employee.getId());
    }
    
    public LeaveBalanceSummaryDto getEmployeeLeaveBalance(String employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        return getEmployeeBalanceSummaryByUser(employee);
    }

    public LeaveBalanceSummaryDto getEmployeeBalanceSummary(String employeeEmail) {
        User employee = userRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        return getEmployeeBalanceSummaryByUser(employee);
    }

    private LeaveBalanceSummaryDto getEmployeeBalanceSummaryByUser(User employee) {
                
        int currentYear = LocalDate.now().getYear();
        
        // Initialize balances if they don't exist
        initializeEmployeeBalances(employee, currentYear);
        
        List<LeaveBalance> balances = leaveBalanceRepo.findByEmployee_Id(employee.getId());
        List<LeaveBalanceDto> balanceDtos = new ArrayList<>();
        
        int totalAllowance = 0;
        int totalUsed = 0;
        int totalRemaining = 0;
        int totalPending = 0;
        
        for (LeaveBalance balance : balances) {
            int usedDays = calculateUsedDays(employee, balance.getLeaveType(), currentYear);
            int pendingDays = calculatePendingDays(employee, balance.getLeaveType(), currentYear);
            
            LeaveBalanceDto dto = LeaveBalanceDto.fromEntity(balance, usedDays, pendingDays);
            balanceDtos.add(dto);
            
            totalAllowance += dto.getTotalAllowance();
            totalUsed += usedDays;
            totalRemaining += balance.getRemainingDays();
            totalPending += pendingDays;
        }
        
        return LeaveBalanceSummaryDto.builder()
                .totalAllowance(totalAllowance)
                .totalUsed(totalUsed)
                .totalRemaining(totalRemaining)
                .totalPending(totalPending)
                .balancesByType(balanceDtos)
                .currentYear(currentYear)
                .build();
    }
    
    private void initializeEmployeeBalances(User employee, int year) {
        YearMonth currentPeriod = YearMonth.of(year, 1);
        
        for (Map.Entry<LeaveType, Integer> entry : DEFAULT_BALANCES.entrySet()) {
            Optional<LeaveBalance> existing = leaveBalanceRepo
                    .findByEmployeeAndLeaveType(employee, entry.getKey());
                    
            if (existing.isEmpty()) {
                LeaveBalance balance = new LeaveBalance();
                balance.setEmployee(employee);
                balance.setLeaveType(entry.getKey());
                balance.setRemainingDays(entry.getValue());
                balance.setPeriod(currentPeriod);
                leaveBalanceRepo.save(balance);
            }
        }
    }
    
    private int calculateUsedDays(User employee, LeaveType leaveType, int year) {
        List<LeaveRequest> approvedRequests = leaveRequestRepository
                .findByEmployeeAndStatusAndYear(employee, LeaveStatus.ACCEPTED, year);
                
        return approvedRequests.stream()
                .filter(req -> getLeaveTypeFromString(req.getType()) == leaveType)
                .mapToInt(this::calculateDaysBetween)
                .sum();
    }
    
    private int calculatePendingDays(User employee, LeaveType leaveType, int year) {
        List<LeaveRequest> pendingRequests = leaveRequestRepository
                .findByEmployeeAndStatusAndYear(employee, LeaveStatus.PENDING, year);
                
        return pendingRequests.stream()
                .filter(req -> getLeaveTypeFromString(req.getType()) == leaveType)
                .mapToInt(this::calculateDaysBetween)
                .sum();
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
    
    private int calculateDaysBetween(LeaveRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        
        // Calculate working days (excluding weekends)
        int totalDays = 0;
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            // Skip weekends (Saturday = 6, Sunday = 7)
            if (current.getDayOfWeek().getValue() < 6) {
                totalDays++;
            }
            current = current.plusDays(1);
        }
        
        // Handle half days
        if (request.isHalfDay() && totalDays > 0) {
            return Math.max(1, totalDays) / 2; // Minimum 0.5 day counted as 1
        }
        
        return totalDays;
    }

    public LeaveBalance updateBalance(Long id, int newDays) {
        LeaveBalance balance = leaveBalanceRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Leave balance not found"));

        balance.setRemainingDays(newDays);
        return leaveBalanceRepo.save(balance);
    }

    public void deductLeave(User employee, LeaveType type, int days) {
        LeaveBalance balance = leaveBalanceRepo
                .findByEmployeeAndLeaveType(employee, type)
                .orElseThrow(() -> new IllegalStateException("No balance found"));

        balance.setRemainingDays(Math.max(0, balance.getRemainingDays() - days));
        leaveBalanceRepo.save(balance);
    }

}
