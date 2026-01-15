package com.rikai.backend.service.user;

import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {
    Page<UserResponse> getAllMentorUsers(Pageable pageable);

    UserResponse createUser(UserCreationRequest userCreateDTO);

    UserResponse updateUser(UUID id, UserUpdateRequest userUpdateDTO);

    void toggleStatus(UUID id);
}
