package com.rikai.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikai.backend.common.CookieUtils;
import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.config.WebMvcConfig;
import com.rikai.backend.controller.AuthController;
import com.rikai.backend.controller.InternController;
import com.rikai.backend.controller.UserController;
import com.rikai.backend.dto.request.user.ChangePasswordRequest;
import com.rikai.backend.exception.GlobalExceptionHandler;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.IAuthenticationService;
import com.rikai.backend.service.intern.IInternService;
import com.rikai.backend.service.token.ITokenService;
import com.rikai.backend.service.user.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, AuthController.class, InternController.class})
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class, WebMvcConfig.class})
class PasswordChangeRequiredInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @MockBean
    private IAuthenticationService authenticationService;

    @MockBean
    private ITokenService tokenService;

    @MockBean
    private IInternService internService;

    @MockBean
    private CookieUtils cookieUtils;

    @MockBean
    private CustomJwtDecoder customJwtDecoder;

    @Test
    @DisplayName("Token with must_change_password = true calling regular API (GET /interns/export) returns 403 PASSWORD_CHANGE_REQUIRED in ApiResponse format")
    void preHandle_MustChangePasswordTrue_RegularApi_Returns403PasswordChangeRequired() throws Exception {
        mockMvc.perform(get("/interns/export")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .subject("admin@rikai.com")
                                        .claim("must_change_password", true))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_CHANGE_REQUIRED.name()))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_CHANGE_REQUIRED.getMessage()));

        verifyNoInteractions(internService);
    }

    @Test
    @DisplayName("Token with must_change_password = true calling GET /users/get-my-info is allowed through")
    void preHandle_MustChangePasswordTrue_GetMyInfo_Allowed() throws Exception {
        Users user = Users.builder()
                .id(UUID.randomUUID())
                .email("mentor@rikai.com")
                .fullName("Mentor")
                .isActive(true)
                .mustChangePassword(true)
                .role(Roles.builder().roleName("MENTOR").build())
                .build();
        when(authenticationService.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/users/get-my-info")
                        .with(jwt().jwt(builder -> builder
                                        .subject("mentor@rikai.com")
                                        .claim("must_change_password", true))
                                .authorities(new SimpleGrantedAuthority("ROLE_MENTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mustChangePassword").value(true));
    }

    @Test
    @DisplayName("Token with must_change_password = true calling POST /users/change-password is allowed through")
    void preHandle_MustChangePasswordTrue_ChangePassword_Allowed() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        mockMvc.perform(post("/users/change-password")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .subject("mentor@rikai.com")
                                        .claim("must_change_password", true))
                                .authorities(new SimpleGrantedAuthority("ROLE_MENTOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("Token with must_change_password = true calling POST /auth/logout is allowed through")
    void preHandle_MustChangePasswordTrue_AuthLogout_Allowed() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .subject("mentor@rikai.com")
                                        .claim("must_change_password", true))
                                .authorities(new SimpleGrantedAuthority("ROLE_MENTOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Token with must_change_password = true calling non-allowlisted user endpoint (POST /users/update-my-info) is blocked with 403")
    void preHandle_MustChangePasswordTrue_NonAllowlistedUserEndpoint_Returns403() throws Exception {
        mockMvc.perform(post("/users/update-my-info")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .subject("mentor@rikai.com")
                                        .claim("must_change_password", true))
                                .authorities(new SimpleGrantedAuthority("ROLE_MENTOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_CHANGE_REQUIRED.name()));
    }

}
