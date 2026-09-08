package com.example.auth.auth.controller;

import com.example.auth.auth.dto.*;
import com.example.auth.auth.service.AuthService;
import com.example.auth.exception.EmailAlreadyExistsException;
import com.example.auth.exception.InvalidResetTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer slice test: the security filter chain is disabled here (see
 * {@code @AutoConfigureMockMvc(addFilters = false)}) because request-level
 * authentication/authorization behaviour is already covered end-to-end by
 * {@code AuthFlowIntegrationTest}. This class focuses purely on request
 * mapping, validation, and response/error shaping for AuthController.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("returns 201 with the created user on success")
        void returns201OnSuccess() throws Exception {
            UserResponse userResponse = new UserResponse("1", "Sanket", "sanket@example.com", "USER", Set.of("USER"));
            when(authService.register(any(RegisterRequest.class), any(), any()))
                    .thenReturn(new AuthResult(userResponse));

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Sanket", "email", "sanket@example.com", "password", "password123"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user.email").value("sanket@example.com"));
        }

        @Test
        @DisplayName("returns 422 with field errors when the payload fails validation")
        void returns422OnValidationFailure() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "S", "email", "not-an-email", "password", "short"))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("returns 409 when the email is already registered")
        void returns409OnDuplicateEmail() throws Exception {
            when(authService.register(any(RegisterRequest.class), any(), any()))
                    .thenThrow(new EmailAlreadyExistsException());

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("name", "Sanket", "email", "taken@example.com", "password", "password123"))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("returns 200 with the authenticated user on success")
        void returns200OnSuccess() throws Exception {
            UserResponse userResponse = new UserResponse("1", "Sanket", "sanket@example.com", "USER", Set.of("USER"));
            when(authService.login(any(LoginRequest.class), any(), any()))
                    .thenReturn(new AuthResult(userResponse));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", "sanket@example.com", "password", "password123"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email").value("sanket@example.com"));
        }

        @Test
        @DisplayName("returns 401 when the credentials are invalid")
        void returns401OnBadCredentials() throws Exception {
            when(authService.login(any(LoginRequest.class), any(), any()))
                    .thenThrow(new BadCredentialsException("bad credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("email", "sanket@example.com", "password", "wrong-password"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class Me {

        @Test
        @DisplayName("returns the current user for the authenticated principal")
        void returnsCurrentUser() throws Exception {
            UserResponse userResponse = new UserResponse("1", "Sanket", "sanket@example.com", "USER", Set.of("USER"));
            Authentication authentication = UsernamePasswordAuthenticationToken
                    .authenticated("sanket@example.com", "password123", List.of());
            when(authService.currentUser(any())).thenReturn(userResponse);

            mockMvc.perform(get("/api/v1/auth/me").principal(authentication))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("sanket@example.com"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/forgot-password")
    class ForgotPassword {

        @Test
        @DisplayName("always returns a generic confirmation message")
        void returnsGenericMessage() throws Exception {
            mockMvc.perform(post("/api/v1/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", "sanket@example.com"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(
                            "If the account exists, password reset instructions have been sent."));

            verify(authService).forgotPassword(new ForgotPasswordRequest("sanket@example.com"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("returns 200 with a success message when the token is valid")
        void returns200OnSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("token", "valid-token", "newPassword", "newPassword123"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password has been reset successfully."));
        }

        @Test
        @DisplayName("returns 400 when the token is invalid or expired")
        void returns400OnInvalidToken() throws Exception {
            doThrow(new InvalidResetTokenException())
                    .when(authService).resetPassword(any(ResetPasswordRequest.class));

            mockMvc.perform(post("/api/v1/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    Map.of("token", "bad-token", "newPassword", "newPassword123"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_RESET_TOKEN"));
        }
    }
}
