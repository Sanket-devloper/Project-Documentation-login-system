package com.example.auth.user.service;

import com.example.auth.exception.EmailAlreadyExistsException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.role.entity.Role;
import com.example.auth.role.repository.RoleRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates a new user, normalizes email, hashes password and assigns the USER role when it already exists")
        void registersUserWithExistingRole() {
            when(userRepository.existsByEmailIgnoreCase("sanket@example.com")).thenReturn(false);
            Role userRole = new Role("USER");
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
            when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.register("  Sanket  ", "  Sanket@Example.com  ", "password123");

            assertThat(result.getName()).isEqualTo("Sanket");
            assertThat(result.getEmail()).isEqualTo("sanket@example.com");
            assertThat(result.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(result.getRoles()).containsExactly(userRole);

            verify(roleRepository, never()).save(any(Role.class));
            verify(passwordEncoder).encode("password123");

            ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(savedUser.capture());
            assertThat(savedUser.getValue().getEmail()).isEqualTo("sanket@example.com");
        }

        @Test
        @DisplayName("creates the USER role on the fly when it does not exist yet")
        void registersUserAndCreatesRoleWhenMissing() {
            when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.register("New User", "new@example.com", "password123");

            verify(roleRepository).save(any(Role.class));
            assertThat(result.getRoles())
                    .extracting(Role::getName)
                    .containsExactly("USER");
        }

        @Test
        @DisplayName("throws EmailAlreadyExistsException when the email is already registered")
        void throwsWhenEmailAlreadyExists() {
            when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register("Name", "taken@example.com", "password123"))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
            verifyNoInteractions(roleRepository, passwordEncoder);
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("returns the user when found")
        void returnsUserWhenFound() {
            User user = new User();
            user.setEmail("found@example.com");
            when(userRepository.findByEmailIgnoreCase("found@example.com")).thenReturn(Optional.of(user));

            User result = userService.findByEmail("found@example.com");

            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("throws UserNotFoundException when no user matches")
        void throwsWhenNotFound() {
            when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
