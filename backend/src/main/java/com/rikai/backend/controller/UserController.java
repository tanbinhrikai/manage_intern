package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.user.UserCreateDTO;
import com.rikai.backend.dto.request.user.UserUpdateDTO;
import com.rikai.backend.dto.response.PageResponse;
import com.rikai.backend.dto.response.user.UserListResponse;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.model.Users;
import com.rikai.backend.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    private final IUserService userService;

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<PageResponse<UserResponse>> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(page, limit);
        PageResponse<UserResponse> result = userService.getAllMentorUsers(pageable);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_USERS_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        Users createdUser = userService.createUser(userCreateDTO);
        return ApiResponse.buildSuccessResponse(
                UserResponse.fromUser(createdUser),
                SuccessCode.CREATE_USER_SUCCESSFUL
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UserResponse> updateUser(@PathVariable("id") String id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        Users updatedUser = userService.updateUser(UUID.fromString(id), userUpdateDTO);
        return ApiResponse.buildSuccessResponse(
                UserResponse.fromUser(updatedUser),
                SuccessCode.UPDATE_USER_SUCCESSFUL
        );
    }

    @PostMapping("/status/{id}/{active}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<UserResponse> changeUserStatus(@Valid @PathVariable UUID id,
                                                      @Valid @PathVariable int active) {
        userService.changeStatus(java.util.UUID.fromString(String.valueOf(id)), active > 0);
        return ApiResponse.buildSuccessResponse(
                null,
                SuccessCode.UPDATE_USER_SUCCESSFUL
        );
    }
}
