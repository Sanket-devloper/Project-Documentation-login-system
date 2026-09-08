package com.example.auth.password.service;

import com.example.auth.exception.InvalidResetTokenException;
import com.example.auth.password.entity.PasswordResetToken;
import com.example.auth.password.repository.PasswordResetTokenRepository;
import com.example.auth.user.entity.User;
import com.example.auth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService")
class PasswordResetServiceTest {

    private static final String FRONTEND_URL = "http://localhost:5173";
    private static final long TTL_MINUTES = 30;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, FRONTEND_URL, TTL_MINUTES);
    }

    @Nested
    @DisplayName("requestReset")
    class RequestReset {

        @Test
        @DisplayName("deletes previous tokens and saves a fresh token when the user exists")
        void createsTokenWhenUserExists() {
            User user = new User();
            user.setEmail("user@example.com");
            when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

            passwordResetService.requestReset("user@example.com");

            verify(tokenRepository).deleteAllByUser(user);

            ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(tokenRepository).save(captor.capture());

            PasswordResetToken savedToken = captor.getValue();
            assertThat(savedToken.getUser()).isSameAs(user);
            assertThat(savedToken.getTokenHash()).isNotBlank();
            assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());
            assertThat(savedToken.getExpiresAt()).isBeforeOrEqualTo(Instant.now().plus(Duration.ofMinutes(TTL_MINUTES + 1)));
        }

        @Test
        @DisplayName("does nothing when no user matches the email (avoids leaking account existence)")
        void doesNothingWhenUserMissing() {
            when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

            passwordResetService.requestReset("missing@example.com");

            verifyNoInteractions(tokenRepository);
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("updates the password and marks the token as used when it is valid")
        void resetsPasswordForValidToken() {
            User user = new User();
            user.setPasswordHash("old-hash");

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));

            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
            when(passwordEncoder.encode("newPassword123")).thenReturn("new-hash");

            passwordResetService.resetPassword("raw-token", "newPassword123");

            assertThat(user.getPasswordHash()).isEqualTo("new-hash");
            assertThat(token.getUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("throws InvalidResetTokenException when the token does not exist")
        void throwsWhenTokenNotFound() {
            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetService.resetPassword("unknown-token", "newPassword123"))
                    .isInstanceOf(InvalidResetTokenException.class);

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("throws InvalidResetTokenException when the token has already been used")
        void throwsWhenTokenAlreadyUsed() {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(new User());
            token.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
            token.setUsedAt(Instant.now().minus(Duration.ofMinutes(1)));

            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> passwordResetService.resetPassword("used-token", "newPassword123"))
                    .isInstanceOf(InvalidResetTokenException.class);
        }

        @Test
        @DisplayName("throws InvalidResetTokenException when the token has expired")
        void throwsWhenTokenExpired() {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(new User());
            token.setExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));

            when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", "newPassword123"))
                    .isInstanceOf(InvalidResetTokenException.class);
        }
    }
}
