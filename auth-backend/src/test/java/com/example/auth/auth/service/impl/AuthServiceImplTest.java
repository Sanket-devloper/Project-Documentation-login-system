package com.example.auth.auth.service.impl;

import com.example.auth.auth.dto.*;
import com.example.auth.auth.mapper.AuthMapper;
import com.example.auth.password.service.PasswordResetService;
import com.example.auth.user.entity.User;
import com.example.auth.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthMapper mapper;

    private AuthServiceImpl authService;

    private MockHttpServletRequest httpRequest;
    private MockHttpServletResponse httpResponse;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userService, passwordResetService, authenticationManager, mapper);
        httpRequest = new MockHttpServletRequest();
        httpResponse = new MockHttpServletResponse();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("authenticates the credentials, stores the security context and returns the mapped user")
        void authenticatesAndReturnsUser() {
            LoginRequest request = new LoginRequest("user@example.com", "password123");

            Authentication authenticatedToken = UsernamePasswordAuthenticationToken
                    .authenticated("user@example.com", "password123", java.util.List.of());
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authenticatedToken);

            User user = new User();
            user.setEmail("user@example.com");
            when(userService.findByEmail("user@example.com")).thenReturn(user);

            UserResponse mappedResponse = new UserResponse("1", "User", "user@example.com", "USER", java.util.Set.of("USER"));
            when(mapper.toResponse(user)).thenReturn(mappedResponse);

            AuthResult result = authService.login(request, httpRequest, httpResponse);

            assertThat(result.user()).isEqualTo(mappedResponse);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authenticatedToken);
            assertThat(httpRequest.getSession(false)).isNotNull();
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("registers the user then logs them in with the same credentials")
        void registersThenLogsIn() {
            RegisterRequest request = new RegisterRequest("New User", "new@example.com", "password123");

            Authentication authenticatedToken = UsernamePasswordAuthenticationToken
                    .authenticated("new@example.com", "password123", java.util.List.of());
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authenticatedToken);

            User user = new User();
            user.setEmail("new@example.com");
            when(userService.findByEmail("new@example.com")).thenReturn(user);

            UserResponse mappedResponse = new UserResponse("2", "New User", "new@example.com", "USER", java.util.Set.of("USER"));
            when(mapper.toResponse(user)).thenReturn(mappedResponse);

            AuthResult result = authService.register(request, httpRequest, httpResponse);

            verify(userService).register("New User", "new@example.com", "password123");
            assertThat(result.user()).isEqualTo(mappedResponse);
        }
    }

    @Test
    @DisplayName("currentUser returns the mapped response for the authenticated principal's email")
    void currentUserReturnsMappedResponse() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");

        User user = new User();
        user.setEmail("user@example.com");
        when(userService.findByEmail("user@example.com")).thenReturn(user);

        UserResponse mappedResponse = new UserResponse("1", "User", "user@example.com", "USER", java.util.Set.of("USER"));
        when(mapper.toResponse(user)).thenReturn(mappedResponse);

        UserResponse result = authService.currentUser(authentication);

        assertThat(result).isEqualTo(mappedResponse);
    }

    @Test
    @DisplayName("forgotPassword delegates to PasswordResetService with the request email")
    void forgotPasswordDelegates() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");

        authService.forgotPassword(request);

        verify(passwordResetService).requestReset("user@example.com");
    }

    @Test
    @DisplayName("resetPassword delegates to PasswordResetService with the token and new password")
    void resetPasswordDelegates() {
        ResetPasswordRequest request = new ResetPasswordRequest("token-value", "newPassword123");

        authService.resetPassword(request);

        verify(passwordResetService).resetPassword("token-value", "newPassword123");
    }
}
