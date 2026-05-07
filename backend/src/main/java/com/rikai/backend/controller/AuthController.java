package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.CookieUtils;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.authentication.AuthenticationRequest;
import com.rikai.backend.dto.response.authentication.AuthenticationResponse;
import com.rikai.backend.service.auth.IAuthenticationService;
import com.rikai.backend.service.token.ITokenService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    IAuthenticationService authenticationService;
    ITokenService tokenService;
    CookieUtils cookieUtils;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse response) {
        var result = authenticationService.authenticate(request);
        cookieUtils.setCookies(response, result.getAccessToken(), result.getRefreshToken());
        return ApiResponse.buildSuccessResponse(result, SuccessCode.LOGIN_SUCCESSFUL);
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse response) {
        var result = tokenService.refreshToken(refreshToken);
        cookieUtils.setAccessCookie(response, result.getAccessToken());
        return ApiResponse.buildSuccessResponse(result, SuccessCode.REFRESH_TOKEN_SUCCESSFUL);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }
        cookieUtils.clearCookies(response);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.LOGOUT_SUCCESSFUL);
    }
}
