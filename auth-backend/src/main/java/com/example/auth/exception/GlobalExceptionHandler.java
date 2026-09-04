package com.example.auth.exception;

import com.example.auth.common.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> badCredentials() {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password.", Map.of());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> duplicateEmail(EmailAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    ResponseEntity<ErrorResponse> invalidResetToken(InvalidResetTokenException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "Please correct the invalid fields.", fields);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflict(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "The request conflicts with existing data.", Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong.", Map.of());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message, Map<String, String> errors) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(Instant.now(), status.value(), code, message, errors));
    }
}
