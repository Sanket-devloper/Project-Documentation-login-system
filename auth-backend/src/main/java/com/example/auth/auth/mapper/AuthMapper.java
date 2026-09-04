package com.example.auth.auth.mapper;

import com.example.auth.auth.dto.UserResponse;
import com.example.auth.role.entity.Role;
import com.example.auth.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthMapper {
    public UserResponse toResponse(User user) {
        log.info("Map the data:");
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String primaryRole = roles.stream().findFirst().orElse("USER");
        return new UserResponse(String.valueOf(user.getId()), user.getName(), user.getEmail(), primaryRole, roles);
    }
}
