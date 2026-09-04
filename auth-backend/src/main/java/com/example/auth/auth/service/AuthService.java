package com.example.auth.auth.service;

import com.example.auth.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    AuthResult register(
            RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    AuthResult login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    UserResponse currentUser(Authentication authentication);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}