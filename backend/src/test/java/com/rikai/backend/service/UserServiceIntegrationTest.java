package com.rikai.backend.service;

import com.rikai.backend.dto.request.user.ChangePasswordRequest;
import com.rikai.backend.model.RefreshToken;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RefreshTokenRepository;
import com.rikai.backend.repository.RolesRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import com.rikai.backend.service.user.IUserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private UsersRepository usersRepository;

    @SpyBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private Users user1;
    private Users user2;
    private RefreshToken token1;
    private RefreshToken token2;

    @BeforeEach
    void setUp() {
        Roles testRole = rolesRepository.findById("MENTOR").orElseGet(() ->
            rolesRepository.save(Roles.builder().roleName("MENTOR").description("Mentor").build())
        );

        user1 = Users.builder()
            .email("test_user1_" + UUID.randomUUID() + "@example.com")
            .passwordHash(passwordEncoder.encode("OldPass123"))
            .fullName("User One")
            .dateOfBirth(LocalDate.of(2000, 1, 1))
            .role(testRole)
            .isActive(true)
            .build();
        user1 = usersRepository.save(user1);

        user2 = Users.builder()
            .email("test_user2_" + UUID.randomUUID() + "@example.com")
            .passwordHash(passwordEncoder.encode("OldPass123"))
            .fullName("User Two")
            .dateOfBirth(LocalDate.of(2000, 2, 2))
            .role(testRole)
            .isActive(true)
                .build();
        user2 = usersRepository.save(user2);

        token1 = RefreshToken.builder()
            .refreshToken("token-user1-" + UUID.randomUUID())
            .expiryDate(Instant.now().plusSeconds(86400))
            .user(user1)
            .build();
        token1 = refreshTokenRepository.save(token1);

        token2 = RefreshToken.builder()
            .refreshToken("token-user2-" + UUID.randomUUID())
            .expiryDate(Instant.now().plusSeconds(86400))
            .user(user2)
            .build();
        token2 = refreshTokenRepository.save(token2);
    }

    // clear data after each test
    @AfterEach
    void tearDown() {
        reset(refreshTokenRepository);
        if (token1 != null && token1.getId() != null) {
            refreshTokenRepository.findById(token1.getId()).ifPresent(refreshTokenRepository::delete);
        }
        if (token2 != null && token2.getId() != null) {
            refreshTokenRepository.findById(token2.getId()).ifPresent(refreshTokenRepository::delete);
        }
        if (user1 != null && user1.getId() != null) {
            refreshTokenRepository.deleteByUser(user1);
            usersRepository.deleteById(user1.getId());
        }
        if (user2 != null && user2.getId() != null) {
            refreshTokenRepository.deleteByUser(user2);
            usersRepository.deleteById(user2.getId());
        }
    }

    @Test
    @DisplayName("Change password in real DB: deletes current user's refresh token and keeps other user's refresh token intact")
    void changePassword_RevokesOnlyCurrentUserTokens_OtherUserTokensIntact() {
        when(authenticationService.getCurrentUser()).thenReturn(user1);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
            .oldPassword("OldPass123")
            .newPassword("NewPass123")
            .confirmPassword("NewPass123")
            .build();

        userService.changePassword(request);

        // Verify user1 password updated in DB
        entityManager.clear();
        Users updatedUser1 = usersRepository.findById(user1.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("NewPass123", updatedUser1.getPasswordHash()));

        // Verify user1 token is deleted from DB
        assertTrue(refreshTokenRepository.findByRefreshToken(token1.getRefreshToken()).isEmpty());

        // Verify user2 token remains intact in DB
        assertTrue(refreshTokenRepository.findByRefreshToken(token2.getRefreshToken()).isPresent());
        Users user2InDb = usersRepository.findById(user2.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("OldPass123", user2InDb.getPasswordHash()));
    }

    @Test
    @DisplayName("Change password fails on token revocation: transaction rolls back and passwordHash in DB remains unchanged")
    void changePassword_TokenRevocationFails_RollsBackPasswordChangeInDb() {
        when(authenticationService.getCurrentUser()).thenReturn(user1);
        String initialPasswordHash = user1.getPasswordHash();

        // Spy repository to simulate failure during token deletion
        doThrow(new RuntimeException("Simulated DB error during token deletion"))
            .when(refreshTokenRepository).deleteByUser(any(Users.class));

        ChangePasswordRequest request = ChangePasswordRequest.builder()
            .oldPassword("OldPass123")
            .newPassword("NewPass123")
            .confirmPassword("NewPass123")
            .build();

        assertThrows(RuntimeException.class, () -> userService.changePassword(request));

        // Clear persistence context to ensure reading fresh data from DB
        entityManager.clear();
        Users persistedUser = usersRepository.findById(user1.getId()).orElseThrow();

        // Password hash must NOT have changed in DB due to transaction rollback
        assertEquals(initialPasswordHash, persistedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("OldPass123", persistedUser.getPasswordHash()));
        assertFalse(passwordEncoder.matches("NewPass123", persistedUser.getPasswordHash()));

        // Reset spy before querying repository again
        reset(refreshTokenRepository);

        // Refresh token of user1 should also still exist due to transaction rollback
        assertTrue(refreshTokenRepository.findByRefreshToken(token1.getRefreshToken()).isPresent());
        // Refresh token of user2 should also still exist
        assertTrue(refreshTokenRepository.findByRefreshToken(token2.getRefreshToken()).isPresent());
    }
}

