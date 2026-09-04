package com.example.auth.exception;
public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() { super("The password reset link is invalid or has expired."); }
}
