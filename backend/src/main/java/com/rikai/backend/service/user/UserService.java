package com.rikai.backend.service.user;

import com.rikai.backend.dto.request.UserCreationRequest;
import com.rikai.backend.dto.request.UserUpdateRequest;
import com.rikai.backend.dto.request.UserStatusUpdateRequest;
import com.rikai.backend.dto.response.UserListResponse;
import com.rikai.backend.dto.response.UserResponse;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public interface UserService {
    UserListResponse getAllUsers();
    UserResponse createUser(UserCreationRequest request);
    UserResponse updateUser(UUID id, UserUpdateRequest request);
    UserResponse changeUserStatus(UserStatusUpdateRequest request);
}
