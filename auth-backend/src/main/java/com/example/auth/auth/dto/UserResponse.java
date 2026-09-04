package com.example.auth.auth.dto;

import java.util.Set;

public record UserResponse(
        String id,
        String name,
        String email,
        String role,
        Set<String> roles
) {}
