package com.rikai.backend.service.user;

import com.rikai.backend.dto.request.UserCreationRequest;
import com.rikai.backend.dto.request.UserUpdateRequest;
import com.rikai.backend.dto.request.UserStatusUpdateRequest;
import com.rikai.backend.dto.response.UserListResponse;
import com.rikai.backend.dto.response.UserResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.RolesRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.common.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements IUserService {
    UsersRepository usersRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    private final RolesRepository rolesRepository;

    @Override
    @Transactional(readOnly = true)
    public UserListResponse getAllUsers() {
        List<UserResponse> users = usersRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
        return UserListResponse.builder()
                .users(users)
                .total(users.size())
                .build();
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Users user = userMapper.toUser(request);
        Optional<Roles> role = rolesRepository.findByRoleName("MENTOR");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        role.ifPresent(user::setRole);
        Users saved = usersRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        Users saved = usersRepository.save(user);
        return userMapper.toUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse changeUserStatus(UserStatusUpdateRequest request) {
        Users user = usersRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setActive(request.isActive());
        Users saved = usersRepository.save(user);
        return userMapper.toUserResponse(saved);
    }
}
