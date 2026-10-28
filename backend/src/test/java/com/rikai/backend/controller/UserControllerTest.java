package com.rikai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikai.backend.common.CookieUtils;
import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.user.ChangePasswordRequest;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.exception.GlobalExceptionHandler;
import com.rikai.backend.model.Enum.RoleType;
import com.rikai.backend.security.CustomJwtDecoder;
import com.rikai.backend.security.JwtAuthenticationEntryPoint;
import com.rikai.backend.security.SecurityConfig;
import com.rikai.backend.service.auth.IAuthenticationService;
import com.rikai.backend.service.user.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @MockBean
    private IAuthenticationService authenticationService;

    @MockBean
    private CookieUtils cookieUtils;

    @MockBean
    private CustomJwtDecoder customJwtDecoder;

    @Test
    @DisplayName("Unauthenticated request to POST /users/change-password returns 401 Unauthorized")
    void changePassword_Unauthenticated_ShouldReturn401() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
        verifyNoInteractions(cookieUtils);
    }

    @Test
    @DisplayName("Authenticated valid request to POST /users/change-password returns 200 and clears cookies")
    @WithMockUser(username = "thanh@gmail.com", roles = "MENTOR")
    void changePassword_Authenticated_ValidRequest_ShouldReturn200AndClearCookies() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        doNothing().when(userService).changePassword(any(ChangePasswordRequest.class));
        doNothing().when(cookieUtils).clearCookies(any(HttpServletResponse.class));

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.CHANGE_PASSWORD_SUCCESSFUL.getMessage()));

        verify(userService).changePassword(any(ChangePasswordRequest.class));
        verify(cookieUtils).clearCookies(any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("Validation failure: blank old password returns 400 Bad Request")
    @WithMockUser(username = "thanh@gmail.com")
    void changePassword_BlankOldPassword_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.OLD_PASSWORD_REQUIRED.getMessage()));

        verifyNoInteractions(userService);
        verifyNoInteractions(cookieUtils);
    }

    @Test
    @DisplayName("Validation failure: weak new password returns 400 Bad Request")
    @WithMockUser(username = "thanh@gmail.com")
    void changePassword_WeakNewPassword_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("weak")
                .confirmPassword("weak")
                .build();

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_WEAK.getMessage()));

        verifyNoInteractions(userService);
        verifyNoInteractions(cookieUtils);
    }

    @Test
    @DisplayName("Validation failure: blank confirm password returns 400 Bad Request")
    @WithMockUser(username = "thanh@gmail.com")
    void changePassword_BlankConfirmPassword_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("NewPass123")
                .confirmPassword("")
                .build();

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONFIRM_PASSWORD_REQUIRED.getMessage()));

        verifyNoInteractions(userService);
        verifyNoInteractions(cookieUtils);
    }

    @Test
    @DisplayName("Validation failure: password exceeding 72 characters returns 400 Bad Request")
    @WithMockUser(username = "thanh@gmail.com")
    void changePassword_PasswordTooLong_ShouldReturn400() throws Exception {
        String longPassword = "A1" + "a".repeat(71); // 73 characters
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword(longPassword)
                .confirmPassword(longPassword)
                .build();

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_TOO_LONG.getMessage()));

        verifyNoInteractions(userService);
        verifyNoInteractions(cookieUtils);
    }

    @Test
    @DisplayName("Service throws PASSWORD_NOT_CORRECT: returns 400 and does not clear cookies")
    @WithMockUser(username = "thanh@gmail.com")
    void changePassword_ServiceThrowsPasswordNotCorrect_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("WrongOldPass123")
                .newPassword("NewPass123")
                .confirmPassword("NewPass123")
                .build();

        doThrow(new AppException(ErrorCode.PASSWORD_NOT_CORRECT))
                .when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_NOT_CORRECT.getMessage()));

        verify(cookieUtils, never()).clearCookies(any());
    }

    @Test
    @DisplayName("Service throws PASSWORD_SAME_AS_OLD: returns 400 and does not clear cookies")
    @WithMockUser(username = "thanh@gmail.com")
    void changePassword_ServiceThrowsPasswordSameAsOld_ShouldReturn400() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("OldPass123")
                .newPassword("OldPass123")
                .confirmPassword("OldPass123")
                .build();

        doThrow(new AppException(ErrorCode.PASSWORD_SAME_AS_OLD))
                .when(userService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_SAME_AS_OLD.getMessage()));

        verify(cookieUtils, never()).clearCookies(any());
    }

    @Test
    @DisplayName("Create user without password: returns 400 PASSWORD_REQUIRED")
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void createUser_MissingPassword_ShouldReturn400() throws Exception {
        UserCreationRequest request = validCreationRequest(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_REQUIRED.name()));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Create user with weak password: returns 400 PASSWORD_WEAK")
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void createUser_WeakPassword_ShouldReturn400() throws Exception {
        UserCreationRequest request = validCreationRequest("weak");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_WEAK.name()));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Create user with password longer than 72 characters: returns 400 PASSWORD_TOO_LONG")
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void createUser_PasswordTooLong_ShouldReturn400() throws Exception {
        UserCreationRequest request = validCreationRequest("Aa1" + "x".repeat(70));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_TOO_LONG.name()));

        verifyNoInteractions(userService);
    }

    private UserCreationRequest validCreationRequest(String password) {
        return UserCreationRequest.builder()
                .password(password)
                .fullName("Nguyen Van A")
                .roleName(RoleType.MENTOR)
                .departmentId(1L)
                .build();
    }
}

