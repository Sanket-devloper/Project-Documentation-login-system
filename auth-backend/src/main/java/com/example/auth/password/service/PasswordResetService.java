package com.example.auth.password.service;

import com.example.auth.exception.InvalidResetTokenException;
import com.example.auth.password.entity.PasswordResetToken;
import com.example.auth.password.repository.PasswordResetTokenRepository;
import com.example.auth.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String frontendUrl;
    private final Duration ttl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
                                @Value("${app.password-reset.ttl-minutes:30}") long ttlMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = frontendUrl;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            tokenRepository.deleteAllByUser(user);
            String rawToken = generateToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(hash(rawToken));
            token.setExpiresAt(Instant.now().plus(ttl));
            tokenRepository.save(token);
            // Development-ready delivery. Replace this log with your email provider in production.
            log.info("Password reset link for {}: {}/reset-password?token={}", user.getEmail(), frontendUrl, rawToken);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidResetTokenException::new);
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidResetTokenException();
        }
        var user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsedAt(Instant.now());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash reset token", e);
        }
    }
}
