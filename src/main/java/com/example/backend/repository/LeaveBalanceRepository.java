package com.example.backend.repository;

import com.example.backend.model.LeaveBalance;
import com.example.backend.model.LeaveType;
import com.example.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployee_Id(String employeeId);
    Optional<LeaveBalance> findByEmployeeAndLeaveType(User employee, LeaveType leaveType);
    @Transactional
    void deleteAllByEmployee(User employee);

}
