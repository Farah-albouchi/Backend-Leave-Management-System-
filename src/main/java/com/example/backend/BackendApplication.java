package com.example.backend;

import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@example.com").isEmpty()) {
				User admin = new User();
				admin.setFirstName("Super");
				admin.setLastName("Admin");
				admin.setEmail("admin@example.com");
				admin.setPassword(passwordEncoder.encode("admin123")); // hashed
				admin.setRole(Role.ADMIN);
				userRepository.save(admin);
				System.out.println("✅ Admin user created.");
			} else {
				System.out.println("✅ Admin user already exists.");
			}
		};
	}
}
