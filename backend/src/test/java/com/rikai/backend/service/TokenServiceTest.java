package com.rikai.backend.service;

import com.nimbusds.jwt.SignedJWT;
import com.rikai.backend.dto.response.authentication.AuthenticationResponse;
import com.rikai.backend.model.RefreshToken;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RefreshTokenRepository;
import com.rikai.backend.service.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private TokenService tokenService;

    private static final String TEST_SIGNER_KEY =
            "1234567890123456789012345678901234567890123456789012345678901234";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(tokenService, "SIGNER_KEY", TEST_SIGNER_KEY);
    }

    @Test
    @DisplayName("generateAccessToken: includes must_change_password = true when user flag is true")
    void generateAccessToken_MustChangePasswordTrue_IncludesTrueClaim() throws ParseException {
        Users user = Users.builder()
                .email("mentor@rikai.com")
                .role(Roles.builder().roleName("MENTOR").build())
                .mustChangePassword(true)
                .build();

        String token = tokenService.generateAccessToken(user);

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals("mentor@rikai.com", signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("ROLE_MENTOR", signedJWT.getJWTClaimsSet().getStringClaim("scope"));
        assertTrue(signedJWT.getJWTClaimsSet().getBooleanClaim("must_change_password"));
    }

    @Test
    @DisplayName("generateAccessToken: includes must_change_password = false when user flag is false")
    void generateAccessToken_MustChangePasswordFalse_IncludesFalseClaim() throws ParseException {
        Users user = Users.builder()
                .email("mentor@rikai.com")
                .role(Roles.builder().roleName("MENTOR").build())
                .mustChangePassword(false)
                .build();

        String token = tokenService.generateAccessToken(user);

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertFalse(signedJWT.getJWTClaimsSet().getBooleanClaim("must_change_password"));
    }

    @Test
    @DisplayName("refreshToken: generates new access token reflecting user's latest mustChangePassword flag from DB")
    void refreshToken_ReflectsLatestUserMustChangePasswordFlag() throws ParseException {
        Users userInDb = Users.builder()
                .email("mentor@rikai.com")
                .role(Roles.builder().roleName("MENTOR").build())
                .mustChangePassword(true)
                .build();

        String rawRefresh = "sample-refresh-token";
        RefreshToken tokenInDb = RefreshToken.builder()
                .refreshToken(tokenService.hashToken(rawRefresh))
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(userInDb)
                .build();

        when(refreshTokenRepository.findByRefreshToken(tokenService.hashToken(rawRefresh)))
                .thenReturn(Optional.of(tokenInDb));

        AuthenticationResponse response = tokenService.refreshToken(rawRefresh);

        SignedJWT signedJWT = SignedJWT.parse(response.getAccessToken());
        assertTrue(signedJWT.getJWTClaimsSet().getBooleanClaim("must_change_password"));
    }
}

