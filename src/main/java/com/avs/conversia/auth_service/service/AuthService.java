package com.avs.conversia.auth_service.service;


import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.avs.conversia.auth_service.dto.AuthResponse;
import com.avs.conversia.auth_service.dto.LoginRequest;
import com.avs.conversia.auth_service.dto.RegisterRequest;
import com.avs.conversia.auth_service.entity.User;
import com.avs.conversia.auth_service.repository.UserRepository;
import com.avs.conversia.tenant_service.entity.Tenant;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String jwtSecret;
    private final long jwtExpiration;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      @Value("${jwt.secret}") String jwtSecret,
                      @Value("${jwt.expiration}") long jwtExpiration) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = generateJwtToken(user);
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setTenantIds(user.getTenants().stream()
                .map(Tenant::getId)
                .collect(Collectors.toSet()));

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Authenticating user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = generateJwtToken(user);
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setTenantIds(user.getTenants().stream()
                .map(Tenant::getId)
                .collect(Collectors.toSet()));

        return response;
    }

    private String generateJwtToken(User user) {
    Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("tenantIds", user.getTenants().stream()
                .map(Tenant::getId)
                .collect(Collectors.toList()))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key, SignatureAlgorithm.HS256) // 👈 FORÇA HS256
            .compact();
}

}