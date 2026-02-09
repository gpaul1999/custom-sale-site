package com.example.authservice.controller;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.exception.TenantIdMissingException;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String TENANT_ID_HEADER = "X-Tenant-ID";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @Valid @RequestBody RegisterRequest request) {
        
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new TenantIdMissingException("X-Tenant-ID header is required");
        }

        logger.info("Register request received for tenant: {}", tenantId);
        AuthResponse response = authService.register(tenantId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @Valid @RequestBody LoginRequest request) {
        
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new TenantIdMissingException("X-Tenant-ID header is required");
        }

        logger.info("Login request received for tenant: {}", tenantId);
        AuthResponse response = authService.login(tenantId, request);
        return ResponseEntity.ok(response);
    }
}
