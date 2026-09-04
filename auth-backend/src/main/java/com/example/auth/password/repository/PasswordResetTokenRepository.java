package com.example.auth.password.repository;

import com.example.auth.password.entity.PasswordResetToken;
import com.example.auth.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteAllByUser(User user);
}
