package com.example.backend.service;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserDto;

public interface UserService {
    UserDto registerUser(RegisterRequest request);

}