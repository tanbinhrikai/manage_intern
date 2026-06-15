package com.rikai.backend.config;

import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RolesRepository;
import com.rikai.backend.repository.UsersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    UsersRepository usersRepository;
    RolesRepository rolesRepository;

    @NonFinal
    @Value("${ADMIN_EMAIL}")
    String ADMIN_EMAIL;

    @NonFinal
    @Value("${ADMIN_PASSWORD}")
    String ADMIN_PASSWORD;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    ApplicationRunner applicationRunner(PasswordEncoder passwordEncoder) {
        return args -> {
            Roles adminRole = rolesRepository.findById("ADMIN").orElseGet(() -> {
                Roles newRole = Roles.builder()
                        .roleName("ADMIN")
                        .description("Admin role")
                        .build();
                return rolesRepository.save(newRole);
            });

            if (rolesRepository.findById("MENTOR").isEmpty()) {
                Roles mentorRole = Roles.builder()
                        .roleName("MENTOR")
                        .description("Mentor role")
                        .build();
                rolesRepository.save(mentorRole);
            }

            if (usersRepository.findByEmail(ADMIN_EMAIL).isEmpty()) {
                Users user = Users.builder()
                        .email(ADMIN_EMAIL)
                        .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(adminRole)
                        .fullName("Admin")
                        .dateOfBirth(LocalDate.of(2004, 10, 15))
                        .build();
                usersRepository.save(user);
                log.warn("admin user has been created");
            }
        };
    }




}