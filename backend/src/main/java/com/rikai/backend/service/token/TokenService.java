package com.rikai.backend.service.token;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.response.AuthenticationResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.RefreshToken;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class TokenService implements ITokenService {

    @NonFinal
    protected long ACCESS_TOKEN_EXPIRY = 900;

    @NonFinal
    protected long REFRESH_TOKEN_EXPIRY = 604800;

    private final RefreshTokenRepository refreshTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Override
    public void saveRefreshToken(Users user, String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .refreshToken(hashToken(refreshToken))
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_EXPIRY, ChronoUnit.SECONDS))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }


    @Override
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
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


    @Override
    public String generateAccessToken(Users user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("rikai.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(ACCESS_TOKEN_EXPIRY, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "ROLE_" + user.getRole().getRoleName())
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

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}
