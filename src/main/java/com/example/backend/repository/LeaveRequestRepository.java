package com.example.backend.repository;

import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByStatusAndEmployeeId(LeaveStatus status, String employeeId);
    List<LeaveRequest> findByEmployeeId(String employeeId);
    
    @Query("SELECT r FROM LeaveRequest r WHERE r.employee = ?1 AND r.status = ?2 AND YEAR(r.startDate) = ?3")
    List<LeaveRequest> findByEmployeeAndStatusAndYear(User employee, LeaveStatus status, int year);
    
    @Query("SELECT TO_CHAR(r.startDate, 'Month YYYY'), COUNT(r) " +
            "FROM LeaveRequest r " +
            "GROUP BY TO_CHAR(r.startDate, 'Month YYYY') " +
            "ORDER BY MIN(r.startDate)")
    List<Object[]> countLeaveRequestsByMonth();

    long countByStatus(LeaveStatus status);
    
    @Query("SELECT COUNT(DISTINCT r.employee) FROM LeaveRequest r " +
           "WHERE r.status = 'ACCEPTED' " +
           "AND :date BETWEEN r.startDate AND r.endDate")
    long countEmployeesOnLeaveForDate(@Param("date") LocalDate date);
    
    @Query("SELECT r FROM LeaveRequest r " +
           "WHERE r.status = 'ACCEPTED' " +
           "AND :date BETWEEN r.startDate AND r.endDate")
    List<LeaveRequest> findEmployeesOnLeaveForDate(@Param("date") LocalDate date);
    
    List<LeaveRequest> findByEmployee(User employee);
    
    @Transactional
    void deleteAllByEmployee(User user);
}
