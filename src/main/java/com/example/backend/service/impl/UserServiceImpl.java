package com.example.backend.service.impl;


import ch.qos.logback.classic.encoder.JsonEncoder;
import com.example.backend.dto.CompleteProfileRequest;
import com.example.backend.dto.CreateEmployeeRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserDto;
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
    public void createEmployee(CreateEmployeeRequest request) {

        String rawPassword = UUID.randomUUID().toString().substring(0, 8);


        String encodedPassword = passwordEncoder.encode(rawPassword);


        User employee = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(Role.EMPLOYEE)
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


        emailService.sendEmail(
                request.getEmail(),
                "Your account has been created",
                "Hello " + request.getFirstName() + " " + request.getLastName() +
                        ",\n\nYour temporary password is: " + rawPassword
        );
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



}