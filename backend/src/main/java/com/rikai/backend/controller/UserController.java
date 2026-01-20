package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.IAuthenticationService;
import com.rikai.backend.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IAuthenticationService authenticationService;
    IUserService userService;

    @GetMapping("/mentors")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> getAllMentorUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(required = false, name = "start_date") LocalDate startDate,
            @RequestParam(required = false, name = "end_date") LocalDate endDate,
            @RequestParam(required = false, name = "department_id") Long departmentId,
            @RequestParam(required = false, name = "is_active") Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageable = PageRequest.of(page, limit);
        PageResponse<UserResponse> result = userService.getAllMentorUsers(pageable, keyword, startDate, endDate, isActive, departmentId);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_USERS_SUCCESSFUL);
    }

    @GetMapping("/hrs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> getAllHrUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(required = false, name = "start_date") LocalDate startDate,
            @RequestParam(required = false, name = "end_date") LocalDate endDate,
            @RequestParam(required = false, name = "department_id") Long departmentId,
            @RequestParam(required = false, name = "is_active") Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageable = PageRequest.of(page, limit);
        PageResponse<UserResponse> result = userService.getAllHrUsers(pageable, keyword, startDate, endDate, isActive, departmentId);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_USERS_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        UserResponse createdUser = userService.createUser(userCreationRequest);
        return ApiResponse.buildSuccessResponse(
                createdUser,
                SuccessCode.CREATE_USER_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(@PathVariable("id") UUID id,
                                                @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        UserResponse updatedUser = userService.updateUser(id, userUpdateRequest);
        return ApiResponse.buildSuccessResponse(
                updatedUser,
                SuccessCode.UPDATE_USER_SUCCESSFUL);
    }

    @PatchMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> toggleUserStatus(@PathVariable UUID id) {
        UserResponse response = userService.toggleStatus(id);
        return ApiResponse.buildSuccessResponse(
                response,
                SuccessCode.UPDATE_USER_SUCCESSFUL
        );
    }

    @GetMapping("/get-my-info")
    public ApiResponse<UserResponse> getMyInfo() {
        Users result = authenticationService.getCurrentUser();
        UserResponse userResponse = UserResponse.fromUser(result);
        return ApiResponse.buildSuccessResponse(userResponse, SuccessCode.GET_MY_INFO_SUCCESSFUL);
    }

    @PostMapping("/update-my-info")
    public ApiResponse<UserResponse> updateSelfMentor(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        UserResponse updatedUser = userService.updateSelfUser(userUpdateRequest);
        return ApiResponse.buildSuccessResponse(
                updatedUser,
                SuccessCode.UPDATE_USER_SUCCESSFUL);
    }
}
