package com.example.backend.service.impl;


import ch.qos.logback.classic.encoder.JsonEncoder;
import com.example.backend.dto.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.backend.model.LeaveBalance;
import com.example.backend.model.LeaveType;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.LeaveBalanceRepository;
import com.example.backend.repository.LeaveRequestRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final EmailService emailService ;
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private NotificationRepository notificationRepository;



    @Override
    public UserDto registerUser(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.EMPLOYEE);

        User savedUser = userRepository.save(user);
        return new UserDto(
                savedUser.getId().toString(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole()
        );

    }
    public User createEmployee(CreateEmployeeRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }
        
        // Generate random password
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Log the generated password for debugging
        System.out.println("🔑 Generated password for " + request.getEmail() + ": " + rawPassword);

        // Build user with provided or default role
        User employee = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .cin(request.getCin())
                .password(encodedPassword)
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                .profileCompleted(false)
                .build();

        User savedEmployee = userRepository.save(employee);


        for (LeaveType type : LeaveType.values()) {
            int defaultDays = switch (type) {
                case VACATION -> 20;
                case SICK -> 10;
                case MATERNITY -> 90;
                case UNPAID -> 0;
            };

            LeaveBalance balance = new LeaveBalance();
            balance.setEmployee(savedEmployee);
            balance.setLeaveType(type);
            balance.setRemainingDays(defaultDays);
            balance.setPeriod(YearMonth.now());

            leaveBalanceRepository.save(balance);
        }


        // Send welcome email with temporary password
        try {
            emailService.sendEmail(
                    request.getEmail(),
                    "Your account has been created",
                    "Hello " + request.getFirstName() + " " + request.getLastName() +
                            ",\n\nYour temporary password is: " + rawPassword +
                            "\n\nPlease log in and complete your profile."
            );
            System.out.println("✅ Email sent successfully to: " + request.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to: " + request.getEmail() + ". Error: " + e.getMessage());
            // Continue without failing the user creation
        }
        
        return savedEmployee;
    }

    // ========== PASSWORD RESET METHOD ==========
    
    @Transactional
    public String resetEmployeePassword(String userId) {
        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
                
        // Generate new random password
        String newRawPassword = UUID.randomUUID().toString().substring(0, 8);
        String newEncodedPassword = passwordEncoder.encode(newRawPassword);
        
        // Update password
        employee.setPassword(newEncodedPassword);
        userRepository.save(employee);
        
        // Log the new password for debugging
        System.out.println("🔑 Reset password for " + employee.getEmail() + ": " + newRawPassword);
        
        // Send email with new password
        try {
            emailService.sendEmail(
                    employee.getEmail(),
                    "Password Reset",
                    "Hello " + employee.getFirstName() + " " + employee.getLastName() +
                            ",\n\nYour new temporary password is: " + newRawPassword +
                            "\n\nPlease log in and complete your profile."
            );
            System.out.println("✅ Password reset email sent to: " + employee.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email to: " + employee.getEmail() + ". Error: " + e.getMessage());
        }
        
        return newRawPassword;
    }

    // ========== NEW EMPLOYEE MANAGEMENT METHODS ==========
    
    public List<User> getAllEmployees() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() != null) // Filter out any invalid users
                .toList();
    }
    
    public User getEmployeeById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }
    
    public User updateEmployee(String id, UpdateEmployeeRequest request) {
        User employee = getEmployeeById(id);
        
        // Update fields if provided
        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress());
        }
        if (request.getCin() != null) {
            employee.setCin(request.getCin());
        }
        if (request.getRole() != null) {
            employee.setRole(request.getRole());
        }
        
        return userRepository.save(employee);
    }
    
    public Map<String, Object> getEmployeeStats() {
        List<User> allUsers = userRepository.findAll();
        
        long totalEmployees = allUsers.stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .count();
                
        long totalAdmins = allUsers.stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .count();
                
        long profileCompleted = allUsers.stream()
                .filter(User::isProfileCompleted)
                .count();
                
        long profilePending = allUsers.size() - profileCompleted;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", totalEmployees);
        stats.put("totalAdmins", totalAdmins);
        stats.put("profileCompleted", profileCompleted);
        stats.put("profilePending", profilePending);
        stats.put("totalUsers", allUsers.size());
        
        return stats;
    }
    public void completeProfile(String email, CompleteProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCin(request.getCin());
        user.setProfileCompleted(true);

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Clean up related entities in correct order
        leaveRequestRepository.deleteAllByEmployee(user);
        leaveBalanceRepository.deleteAllByEmployee(user);
        notificationRepository.deleteAllByRecipient(user);

        userRepository.delete(user);
    }

    // ========== ADMIN PROFILE METHODS ==========
    
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    @Override
    public User updateAdminProfile(String email, UpdateAdminProfileRequest request) {
        User admin = getUserByEmail(email);
        
        // Check if email is being changed and if it's already taken by another user
        if (!admin.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email " + request.getEmail() + " is already taken");
            }
        }
        
        // Update fields
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setAddress(request.getAddress());
        
        if (request.getCin() != null && !request.getCin().trim().isEmpty()) {
            admin.setCin(Long.parseLong(request.getCin()));
        }
        
        // Mark profile as completed if not already
        if (!admin.isProfileCompleted()) {
            admin.setProfileCompleted(true);
        }
        
        return userRepository.save(admin);
    }
    
    @Override
    public void changeAdminPassword(String email, ChangePasswordRequest request) {
        User admin = getUserByEmail(email);
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(admin);
    }

}