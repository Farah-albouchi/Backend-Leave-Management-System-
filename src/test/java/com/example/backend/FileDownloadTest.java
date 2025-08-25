package com.example.backend;

import com.example.backend.controller.AdminController;
import com.example.backend.model.LeaveRequest;
import com.example.backend.model.LeaveStatus;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.service.FileStorageService;
import com.example.backend.service.LeaveRequestService;
import com.example.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileDownloadTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private LeaveRequestService leaveRequestService;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AdminController adminController;

    private UUID requestId;
    private LeaveRequest leaveRequest;
    private User employee;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        
        employee = User.builder()
                .id("emp1")
                .email("employee@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.EMPLOYEE)
                .build();

        leaveRequest = LeaveRequest.builder()
                .id(requestId)
                .employee(employee)
                .type("Annual Leave")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .reason("Vacation")
                .status(LeaveStatus.PENDING)
                .documentPath("uploads/test_document.pdf")
                .submittedAt(LocalDate.now())
                .build();
    }

    @Test
    void testDownloadDocument_Success() {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);
        
        when(leaveRequestService.getRequestById(requestId)).thenReturn(leaveRequest);
        when(fileStorageService.loadFileAsResource("test_document.pdf")).thenReturn(resource);

        // Act
        ResponseEntity<Resource> response = adminController.downloadLeaveRequestDocument(requestId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("test_document.pdf"));
    }

    @Test
    void testDownloadDocument_NoDocument() {
        // Arrange
        leaveRequest.setDocumentPath(null);
        when(leaveRequestService.getRequestById(requestId)).thenReturn(leaveRequest);

        // Act
        ResponseEntity<Resource> response = adminController.downloadLeaveRequestDocument(requestId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDownloadDocument_FileNotFound() {
        // Arrange
        when(leaveRequestService.getRequestById(requestId)).thenReturn(leaveRequest);
        when(fileStorageService.loadFileAsResource(any())).thenThrow(new RuntimeException("File not found"));

        // Act
        ResponseEntity<Resource> response = adminController.downloadLeaveRequestDocument(requestId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
