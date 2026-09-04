package com.example.auth.auth.controller;

import com.example.auth.auth.dto.*;
import com.example.auth.auth.service.AuthService;
import com.example.auth.common.dto.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResult> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        return ResponseEntity.status(201)
                .body(authService.register(
                        request,
                        httpRequest,
                        httpResponse
                ));
    }

    @PostMapping("/login")
    public AuthResult login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        return authService.login(
                request,
                httpRequest,
                httpResponse
        );
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {

        log.info("GET /api/v1/auth/me - Request received");

        return authService.currentUser(authentication);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return new MessageResponse(
                "If the account exists, password reset instructions have been sent."
        );
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return new MessageResponse(
                "Password has been reset successfully."
        );
    }
}