package com.example.backend.service.impl;

import com.example.backend.model.LeaveBalance;
import com.example.backend.model.LeaveType;
import com.example.backend.model.User;
import com.example.backend.repository.LeaveBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class LeaveBalanceServiceImp {


    @Autowired
    private LeaveBalanceRepository leaveBalanceRepo;

    private static final Map<LeaveType, Integer> DEFAULT_BALANCES = Map.of(
            LeaveType.VACATION, 10,
            LeaveType.SICK, 5,
            LeaveType.UNPAID, 0
    );

    public List<LeaveBalance> getBalancesByEmployee(User employee) {
        return leaveBalanceRepo.findByEmployee_Id(employee.getId());
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

        balance.setRemainingDays(balance.getRemainingDays() - days);
        leaveBalanceRepo.save(balance);
    }

}
