package com.example.auth.security;

import com.example.auth.role.entity.Role;
import com.example.auth.user.entity.User;
import com.example.auth.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUserDetailsService")
class SecurityUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private SecurityUserDetailsService detailsService;

    @BeforeEach
    void setUp() {
        detailsService = new SecurityUserDetailsService(userRepository);
    }

    @Test
    @DisplayName("loads a user and maps roles to ROLE_-prefixed authorities")
    void loadsUserWithAuthorities() {
        User user = new User();
        user.setEmail("sanket@example.com");
        user.setPasswordHash("hashed-password");
        user.setEnabled(true);
        Set<Role> roles = new LinkedHashSet<>();
        roles.add(new Role("ADMIN"));
        user.setRoles(roles);

        when(userRepository.findByEmailIgnoreCase("sanket@example.com")).thenReturn(Optional.of(user));

        UserDetails details = detailsService.loadUserByUsername("sanket@example.com");

        assertThat(details.getUsername()).isEqualTo("sanket@example.com");
        assertThat(details.getPassword()).isEqualTo("hashed-password");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("marks the returned UserDetails as disabled when the user account is disabled")
    void marksDisabledUsersAsDisabled() {
        User user = new User();
        user.setEmail("disabled@example.com");
        user.setPasswordHash("hashed-password");
        user.setEnabled(false);
        user.setRoles(new LinkedHashSet<>());

        when(userRepository.findByEmailIgnoreCase("disabled@example.com")).thenReturn(Optional.of(user));

        UserDetails details = detailsService.loadUserByUsername("disabled@example.com");

        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("throws UsernameNotFoundException when no user matches the email")
    void throwsWhenUserMissing() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> detailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
