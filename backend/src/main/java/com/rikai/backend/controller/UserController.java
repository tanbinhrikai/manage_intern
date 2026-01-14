package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.UserCreationRequest;
import com.rikai.backend.dto.request.UserUpdateRequest;
import com.rikai.backend.dto.request.UserStatusUpdateRequest;
import com.rikai.backend.dto.response.UserListResponse;
import com.rikai.backend.dto.response.UserResponse;
import com.rikai.backend.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @GetMapping
    public ApiResponse<UserListResponse> getAllUsers() {
        return ApiResponse.buildSuccessResponse(userService.getAllUsers(), SuccessCode.GET_ALL_USERS_SUCCESSFUL);
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.buildSuccessResponse(userService.createUser(request), SuccessCode.CREATE_USER_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(userService.updateUser(id, request), SuccessCode.UPDATE_USER_SUCCESSFUL);
    }

    @PatchMapping("/status")
    public ApiResponse<UserResponse> changeUserStatus(@Valid @RequestBody UserStatusUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(userService.changeUserStatus(request), SuccessCode.UPDATE_USER_SUCCESSFUL);
    }
}
