package com.example.backend.dto;


import lombok.Data;

@Data
public class CreateEmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
}
