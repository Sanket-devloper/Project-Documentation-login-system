package com.example.auth.config;

import com.example.auth.role.entity.Role;
import com.example.auth.role.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName("USER").isEmpty()) roleRepository.save(new Role("USER"));
            if (roleRepository.findByName("ADMIN").isEmpty()) roleRepository.save(new Role("ADMIN"));
        };
    }
}
