package com.example.authservice.service;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.User;
import com.example.authservice.exception.AuthenticationException;
import com.example.authservice.exception.UserAlreadyExistsException;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(String tenantId, RegisterRequest request) {
        logger.info("Registering user with email: {} for tenant: {}", request.getEmail(), tenantId);

        if (userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .tenantId(tenantId)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        user = userRepository.save(user);
        logger.info("User registered successfully with id: {}", user.getId());

        String token = jwtUtil.generateToken(user.getId().toString(), tenantId);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .tenantId(tenantId)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String tenantId, LoginRequest request) {
        logger.info("Login attempt for email: {} in tenant: {}", request.getEmail(), tenantId);

        User user = userRepository.findByTenantIdAndEmail(tenantId, request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        logger.info("User logged in successfully with id: {}", user.getId());

        String token = jwtUtil.generateToken(user.getId().toString(), tenantId);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .tenantId(tenantId)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
