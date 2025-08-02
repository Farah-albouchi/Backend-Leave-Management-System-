package com.example.backend.controller;

import com.example.backend.dto.CompleteProfileRequest;

import com.example.backend.service.UserService;
import com.example.backend.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserServiceImpl userService;

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestBody CompleteProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.completeProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Profile completed successfully.");
    }
}
