package com.example.auth.auth.mapper;

import com.example.auth.auth.dto.UserResponse;
import com.example.auth.role.entity.Role;
import com.example.auth.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthMapper")
class AuthMapperTest {

    private final AuthMapper mapper = new AuthMapper();

    @Test
    @DisplayName("maps a user's id, name, email and roles, using the first role as the primary role")
    void mapsUserWithRoles() {
        User user = new User();
        user.setName("Sanket");
        user.setEmail("sanket@example.com");
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(new Role("ADMIN"));
        roles.add(new Role("USER"));
        user.setRoles(roles);
        setId(user, 42L);

        UserResponse response = mapper.toResponse(user);

        assertThat(response.id()).isEqualTo("42");
        assertThat(response.name()).isEqualTo("Sanket");
        assertThat(response.email()).isEqualTo("sanket@example.com");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.roles()).containsExactly("ADMIN", "USER");
    }

    @Test
    @DisplayName("defaults the primary role to USER when the user has no roles")
    void defaultsToUserRoleWhenNoneAssigned() {
        User user = new User();
        user.setName("No Roles");
        user.setEmail("noroles@example.com");
        user.setRoles(new LinkedHashSet<>());

        UserResponse response = mapper.toResponse(user);

        assertThat(response.role()).isEqualTo("USER");
        assertThat(response.roles()).isEmpty();
    }

    private static void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
