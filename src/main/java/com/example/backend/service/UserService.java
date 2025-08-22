package com.example.backend.service;

import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.dto.UpdateAdminProfileRequest;
import com.example.backend.dto.ChangePasswordRequest;
import com.example.backend.model.User;

public interface UserService {
    UserDto registerUser(RegisterRequest request);
    User getUserByEmail(String email);
    User updateAdminProfile(String email, UpdateAdminProfileRequest request);
    void changeAdminPassword(String email, ChangePasswordRequest request);
}