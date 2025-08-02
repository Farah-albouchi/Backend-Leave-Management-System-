package com.example.backend.controller;

import com.example.backend.dto.LeaveRequestDto;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.LeaveRequestService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leave")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UserRepository userRepository;

    public LeaveRequestController(LeaveRequestService leaveRequestService, UserRepository userRepository) {
        this.leaveRequestService = leaveRequestService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> requestLeave(
            @RequestPart("dto") LeaveRequestDto dto,
            @RequestPart(value = "document", required = false) MultipartFile document,
            Principal principal
    ) {
        LeaveRequest saved = leaveRequestService.submitLeaveRequest(dto, document, principal.getName());
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequest>> myRequests(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(leaveRequestService.getMyRequests(user.getId()));
    }
    @GetMapping("/my-requests/{id}")
    public ResponseEntity<LeaveRequest> getRequestById(@PathVariable UUID id, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LeaveRequest request = leaveRequestService.getMyRequestById(id, user.getId());
        return ResponseEntity.ok(request);
    }


}
