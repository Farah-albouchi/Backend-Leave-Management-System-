package com.example.backend.dto;

import com.example.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String id;
    private String firstName ;
    private String lastName;
    private Role role;
}