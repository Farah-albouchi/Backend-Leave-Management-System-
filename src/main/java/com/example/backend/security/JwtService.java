package com.example.backend.security;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret; // from application.properties
    private final UserRepository userRepository;
    private SecretKey key;

    @PostConstruct
    public void init() {
        // Ensure the key is secure and long enough for HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();

        // Fetch the user to get the role
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return Jwts.builder()
                .setSubject(username)
                .claim("role", "ROLE_" + user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }



    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build() // ✅ build first
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }
}
