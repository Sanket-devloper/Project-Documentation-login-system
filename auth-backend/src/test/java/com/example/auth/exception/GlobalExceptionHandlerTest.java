package com.example.auth.exception;

import com.example.auth.common.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("maps BadCredentialsException to 401 INVALID_CREDENTIALS")
    void mapsBadCredentials() {
        ResponseEntity<ErrorResponse> response = handler.badCredentials();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("maps EmailAlreadyExistsException to 409 EMAIL_ALREADY_EXISTS")
    void mapsDuplicateEmail() {
        ResponseEntity<ErrorResponse> response = handler.duplicateEmail(new EmailAlreadyExistsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("EMAIL_ALREADY_EXISTS");
        assertThat(response.getBody().message()).isEqualTo("An account with this email already exists.");
    }

    @Test
    @DisplayName("maps InvalidResetTokenException to 400 INVALID_RESET_TOKEN")
    void mapsInvalidResetToken() {
        ResponseEntity<ErrorResponse> response = handler.invalidResetToken(new InvalidResetTokenException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("INVALID_RESET_TOKEN");
    }

    @Test
    @DisplayName("maps DataIntegrityViolationException to 409 DATA_CONFLICT")
    void mapsDataConflict() {
        ResponseEntity<ErrorResponse> response = handler.conflict(new DataIntegrityViolationException("conflict"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("DATA_CONFLICT");
    }

    @Test
    @DisplayName("maps any other exception to 500 INTERNAL_ERROR")
    void mapsGenericException() {
        ResponseEntity<ErrorResponse> response = handler.generic(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }
}
