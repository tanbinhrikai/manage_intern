package com.rikai.backend.service;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.AuthenticationRequest;
import com.rikai.backend.dto.response.AuthenticationResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RefreshTokenRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.service.token.ITokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UsersRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;

    private final ITokenService tokenService;

    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!authenticated)
            throw new AppException(ErrorCode.USERNAME_OR_PASSWORD_INCORRECT);

        var accessToken = tokenService.generateAccessToken(user);
        var refreshToken = UUID.randomUUID().toString();
        tokenService.saveRefreshToken(user, refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userResponse(userMapper.toUserResponse(user))
                .build();
    }



    @Transactional
    public void logout(String refreshToken) {
        var tokenInDB = refreshTokenRepository.findByRefreshToken(tokenService.hashToken(refreshToken));
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

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String userIdentifier = null;
        if (principal instanceof Jwt jwt) {
            userIdentifier = jwt.getSubject();
        }
        else if (principal instanceof UserDetails) {
            userIdentifier = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            userIdentifier = (String) principal;
        }
        if (userIdentifier != null) {
            return userRepository.findByEmail(userIdentifier)
                    .filter(Users::isActive)
                    .orElse(null);
        }
        return null;
    }
}
