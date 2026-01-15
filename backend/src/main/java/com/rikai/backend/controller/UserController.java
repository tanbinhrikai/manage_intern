package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.Users;
import com.rikai.backend.service.auth.AuthenticationService;
import com.rikai.backend.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
        AuthenticationService authenticationService;
        IUserService userService;

        @GetMapping("")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<PageResponse<UserResponse>> getAllUser(
                        @RequestParam(defaultValue = "", required = false) String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int limit) {
                Pageable pageable = PageRequest.of(page, limit);
                Page<UserResponse> userPage = userService.getAllMentorUsers(pageable);

                PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                                .items(userPage.getContent())
                                .currentPage(userPage.getNumber())
                                .totalPages(userPage.getTotalPages())
                                .totalItems(userPage.getTotalElements())
                                .pageSize(userPage.getSize())
                                .build();
                return ApiResponse.buildSuccessResponse(pageResponse, SuccessCode.GET_ALL_USERS_SUCCESSFUL);
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

        @PostMapping("/status/{id}/{active}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<Void> changeUserStatus(@PathVariable UUID id,
                        @PathVariable int active) {
                userService.changeStatus(id, active > 0);
                return ApiResponse.buildSuccessResponse(null, SuccessCode.UPDATE_USER_SUCCESSFUL);
        }

        @GetMapping("/get-my-info")
        public ApiResponse<UserResponse> getMyInfo() {
                Users result = authenticationService.getCurrentUser();
                UserResponse userResponse = UserResponse.fromUser(result);
                return ApiResponse.buildSuccessResponse(userResponse, SuccessCode.GET_MY_INFO_SUCCESSFUL);
        }
}
