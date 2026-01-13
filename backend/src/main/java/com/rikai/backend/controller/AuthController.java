package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.AuthenticationRequest;
import com.rikai.backend.dto.response.AuthenticationResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.service.AuthenticationService;
import com.rikai.backend.service.token.ITokenService;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    AuthenticationService authenticationService;
    private final ITokenService tokenService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        var result = authenticationService.authenticate(request);
        authenticationService.setCookies(response, result.getAccessToken(), result.getRefreshToken());
        return ApiResponse.buildSuccessResponse(result, SuccessCode.LOGIN_SUCCESSFUL);
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var result = tokenService.refreshToken(refreshToken);
        authenticationService.setAccessCookie(response, result.getAccessToken());
        return ApiResponse.buildSuccessResponse(result, SuccessCode.REFRESH_TOKEN_SUCCESSFUL);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }
        authenticationService.clearCookies(response);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.LOGOUT_SUCCESSFUL);
    }
}
