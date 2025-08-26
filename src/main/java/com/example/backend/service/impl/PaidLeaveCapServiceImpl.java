package com.example.backend.service.impl;

import com.example.backend.dto.PaidLeaveCapCheckResult;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.model.User;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.PaidLeaveCapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaidLeaveCapServiceImpl implements PaidLeaveCapService {
    
    private static final int SYSTEM_DEFAULT_PAID_LEAVE_CAP = 30; // 30 days per year
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Override
    public int getEffectivePaidLeaveCap(User employee) {
        return employee.getPaidLeaveCapDays() != null ? 
               (int) employee.getPaidLeaveCapDays() : 
               SYSTEM_DEFAULT_PAID_LEAVE_CAP;
    }
    
    @Override
    public int getTotalPaidLeaveUsed(User employee, int year) {
        // Temporarily use all approved requests until database migration is complete
        List<LeaveRequest> approvedRequests = leaveRequestRepository
                .findByEmployeeAndStatusAndYear(employee, LeaveStatus.ACCEPTED, year);
                
        return approvedRequests.stream()
                // .filter(req -> req.isPaid()) // Temporarily removed - requires database migration
                .mapToInt(this::calculateWorkingDays)
                .sum();
    }
    
    @Override
    public int getRemainingPaidLeaveQuota(User employee, int year) {
        int cap = getEffectivePaidLeaveCap(employee);
        int used = getTotalPaidLeaveUsed(employee, year);
        return Math.max(0, cap - used);
    }
    
    @Override
    public PaidLeaveCapCheckResult checkPaidLeaveCapForRequest(User employee, int requestDays, int year) {
        int remainingQuota = getRemainingPaidLeaveQuota(employee, year);
        
        if (requestDays <= remainingQuota) {
            // Request fits entirely within paid quota
            return PaidLeaveCapCheckResult.builder()
                    .totalRequestDays(requestDays)
                    .paidDays(requestDays)
                    .unpaidDays(0)
                    .remainingQuota(remainingQuota - requestDays)
                    .exceedsQuota(false)
                    .message("Request approved as paid leave")
                    .build();
        } else {
            // Request exceeds quota - split into paid and unpaid
            int paidDays = remainingQuota;
            int unpaidDays = requestDays - remainingQuota;
            
            return PaidLeaveCapCheckResult.builder()
                    .totalRequestDays(requestDays)
                    .paidDays(paidDays)
                    .unpaidDays(unpaidDays)
                    .remainingQuota(0)
                    .exceedsQuota(true)
                    .message(String.format("Request split: %d days paid, %d days unpaid (quota exceeded)", 
                            paidDays, unpaidDays))
                    .build();
        }
    }
    
    @Override
    public void updateEmployeePaidLeaveCap(String employeeId, Integer capDays) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
                
        employee.setPaidLeaveCapDays(capDays);
        userRepository.save(employee);
    }
    
    @Override
    public int getSystemDefaultPaidLeaveCap() {
        return SYSTEM_DEFAULT_PAID_LEAVE_CAP;
    }
    
    private int calculateWorkingDays(LeaveRequest request) {
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
}
