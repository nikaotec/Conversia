package com.avs.conversia.auth_service.service;

import java.security.Key;
import java.util.Date;
import java.util.Set;

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

        // Como o usuário ainda não tem tenants, enviamos lista vazia
        String token = generateJwtToken(user.getEmail(), Set.of());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setTenantIds(Set.of());

        return response;
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Authenticating user with email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Consulta segura para evitar acesso direto a user.getTenants()
        Set<Long> tenantIds = userRepository.findTenantIdsByEmail(user.getEmail());

        String token = generateJwtToken(user.getEmail(), tenantIds);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setTenantIds(tenantIds);

        return response;
    }

    private String generateJwtToken(String email, Set<Long> tenantIds) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(email)
                .claim("tenantIds", tenantIds)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
