package com.rikai.backend.security;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PasswordChangeRequiredInterceptor implements HandlerInterceptor {

    public static final String CLAIM_MUST_CHANGE_PASSWORD = "must_change_password";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return true;
        }

        if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean(CLAIM_MUST_CHANGE_PASSWORD))) {
            return true;
        }

        if (isAllowedRequest(request.getMethod(), getApplicationPath(request))) {
            return true;
        }

        throw new AppException(ErrorCode.PASSWORD_CHANGE_REQUIRED);
    }

    private boolean isAllowedRequest(String method, String path) {
        if ("/auth".equals(path) || path.startsWith("/auth/")) {
            return true;
        }
        if (HttpMethod.GET.matches(method) && "/users/get-my-info".equals(path)) {
            return true;
        }
        return HttpMethod.POST.matches(method) && "/users/change-password".equals(path);
    }

    private String getApplicationPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }
}

