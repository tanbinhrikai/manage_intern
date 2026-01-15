package com.rikai.backend.service.user;

import com.rikai.backend.dto.request.user.UserCreateDTO;
import com.rikai.backend.dto.request.user.UserUpdateDTO;
import com.rikai.backend.dto.response.PageResponse;
import com.rikai.backend.dto.response.user.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {
    PageResponse<UserResponse> getAllMentorUsers(Pageable pageable);

    UserResponse createUser(UserCreateDTO userCreateDTO);
    UserResponse updateUser(UUID id, UserUpdateDTO userUpdateDTO);

    UserResponse toggleStatus(UUID id);
}
