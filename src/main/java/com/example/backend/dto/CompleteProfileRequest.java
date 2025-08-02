package com.example.backend.dto;
import lombok.Data;

@Data
public class CompleteProfileRequest {
    private String phone;
    private String address;
    private Long cin;
}
