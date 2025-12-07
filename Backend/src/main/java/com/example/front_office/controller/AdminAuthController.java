package com.example.front_office.controller;

import com.example.front_office.controller.dto.AdminLoginRequest;
import com.example.front_office.controller.dto.AdminRegisterRequest;
import com.example.front_office.controller.dto.AuthResponse;
import com.example.front_office.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backoffice/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody AdminRegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AdminLoginRequest request
    ) {
        return ResponseEntity.ok(service.login(request));
    }
}