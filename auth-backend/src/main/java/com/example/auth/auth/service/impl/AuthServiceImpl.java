package com.example.auth.auth.service.impl;

import com.example.auth.auth.dto.*;
import com.example.auth.auth.mapper.AuthMapper;
import com.example.auth.auth.service.AuthService;
import com.example.auth.password.service.PasswordResetService;
import com.example.auth.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper mapper;

    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthServiceImpl(
            UserService userService,
            PasswordResetService passwordResetService,
            AuthenticationManager authenticationManager,
            AuthMapper mapper
    ) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.authenticationManager = authenticationManager;
        this.mapper = mapper;
    }

    @Override
    public AuthResult register(
            RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        userService.register(
                request.name(),
                request.email(),
                request.password()
        );

        return login(
                new LoginRequest(
                        request.email(),
                        request.password()
                ),
                httpRequest,
                httpResponse
        );
    }

    @Override
    public AuthResult login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                request.email(),
                                request.password()
                        )
                );

        var context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );

        return new AuthResult(
                mapper.toResponse(
                        userService.findByEmail(authentication.getName())
                )
        );
    }

    @Override
    public UserResponse currentUser(Authentication authentication) {
        log.info("user get Request ");
        return mapper.toResponse(
                userService.findByEmail(authentication.getName())
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        passwordResetService.requestReset(
                request.email()
        );
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );
    }
}