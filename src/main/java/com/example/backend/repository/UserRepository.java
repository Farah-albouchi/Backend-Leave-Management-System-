package com.example.backend.repository;

import com.example.backend.model.Role;
import com.example.backend.model.User;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);


}