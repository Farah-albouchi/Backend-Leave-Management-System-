package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.model.User;

public interface UserService {
    UserDto registerUser(RegisterRequest request);
    User getUserByEmail(String email);
    User updateAdminProfile(String email, UpdateAdminProfileRequest request);
    void changeAdminPassword(String email, ChangePasswordRequest request);
    ProfileDto getUserProfile(String email);
    ProfileDto updateUserProfile(String email, UpdateProfileRequest request);
}