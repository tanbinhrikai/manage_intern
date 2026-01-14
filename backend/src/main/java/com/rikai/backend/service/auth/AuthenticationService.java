package com.rikai.backend.service.auth;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.AuthenticationRequest;
import com.rikai.backend.dto.response.AuthenticationResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.repository.RefreshTokenRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.service.token.ITokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {

    UsersRepository userRepository;
    RefreshTokenRepository refreshTokenRepository;

    private final ITokenService tokenService;

    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @Override
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

    @Override
    @Transactional
    public void logout(String refreshToken) {
        var tokenInDB = refreshTokenRepository.findByRefreshToken(tokenService.hashToken(refreshToken));
        tokenInDB.ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public com.rikai.backend.model.Users getCurrentUser() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        String userIdentifier = null;
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            userIdentifier = jwt.getSubject();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            userIdentifier = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            userIdentifier = (String) principal;
        }
        if (userIdentifier != null) {
            return userRepository.findByEmail(userIdentifier)
                    .filter(com.rikai.backend.model.Users::isActive)
                    .orElse(null);
        }
        return null;
    }
}
