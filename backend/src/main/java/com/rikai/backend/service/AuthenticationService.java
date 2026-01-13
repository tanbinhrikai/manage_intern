package com.rikai.backend.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.AuthenticationRequest;
import com.rikai.backend.dto.response.AuthenticationResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.model.RefreshToken;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RefreshTokenRepository;
import com.rikai.backend.repository.UsersRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UsersRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;

    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    protected long ACCESS_TOKEN_EXPIRY = 900;

    @NonFinal
    protected long REFRESH_TOKEN_EXPIRY = 604800;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!authenticated)
            throw new AppException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);

        var accessToken = generateAccessToken(user);
        var refreshToken = UUID.randomUUID().toString();
        saveRefreshToken(user, refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponse(userMapper.toUserResponse(user))
                .build();
    }

    private void saveRefreshToken(Users user, String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .refreshToken(hashToken(refreshToken))
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_EXPIRY, ChronoUnit.SECONDS))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }

    public AuthenticationResponse refreshToken(String refreshToken) {

        var tokenInDB = refreshTokenRepository.findByRefreshToken(hashToken(refreshToken))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (tokenInDB.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(tokenInDB);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var newAccessToken = generateAccessToken(tokenInDB.getUser());

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        var tokenInDB = refreshTokenRepository.findByRefreshToken(hashToken(refreshToken));
        tokenInDB.ifPresent(refreshTokenRepository::delete);
    }

    public void setAccessCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null)
            return;
        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    public void clearCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
    public void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAccessCookie(response, accessToken);
        setRefreshCookie(response, refreshToken);
    }

    private String generateAccessToken(Users user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("rikai.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(ACCESS_TOKEN_EXPIRY, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "ROLE_" + user.getRole())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
