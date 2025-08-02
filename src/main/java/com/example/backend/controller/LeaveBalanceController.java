package com.example.backend.controller;

import com.example.backend.model.LeaveBalance;
import com.example.backend.model.User;
import com.example.backend.service.impl.LeaveBalanceServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/leave-balance")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceServiceImp balanceService;

    @GetMapping("/{employeeId}")
    public List<LeaveBalance> getEmployeeBalances(@PathVariable String employeeId) {
        User employee = new User();
        employee.setId(employeeId);
        return balanceService.getBalancesByEmployee(employee);
    }



    @PutMapping("/{balanceId}")
    public LeaveBalance updateBalance(
            @PathVariable Long balanceId,
            @RequestParam int newDays
    ) {
        return balanceService.updateBalance(balanceId, newDays);
    }
}
