package com.rikai.backend.service.user;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.UUID;

public interface IUserService {
    PageResponse<UserResponse> getAllMentorUsers(
            PageRequest pageRequest,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive,
            Long departmentId);

    PageResponse<UserResponse> getAllHrUsers(
            PageRequest pageRequest,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive,
            Long departmentId);

    UserResponse createUser(UserCreationRequest userCreateDTO);

    UserResponse updateUser(UUID id, UserUpdateRequest userUpdateDTO);

    UserResponse updateSelfUser(UserUpdateRequest userUpdateDTO);

    UserResponse toggleStatus(UUID id);
}
