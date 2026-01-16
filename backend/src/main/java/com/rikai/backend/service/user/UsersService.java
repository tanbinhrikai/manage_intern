package com.rikai.backend.service.user;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.model.Department;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.DepartmentRepository;
import com.rikai.backend.repository.RolesRepository;
import com.rikai.backend.repository.UsersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UsersService implements IUserService {
    UsersRepository usersRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RolesRepository rolesRepository;
    DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllMentorUsers(Pageable pageable) {
        return usersRepository.findAllMentorUsers(pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest userCreateDTO) {
        if (usersRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Users user = userMapper.toUser(userCreateDTO);
        user.setActive(true);
        Roles role = rolesRepository.findByRoleName("MENTOR")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setPasswordHash(passwordEncoder.encode(userCreateDTO.getPassword()));
        user.setRole(role);
        if (userCreateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userCreateDTO.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));
            user.setDepartment(department);
        }
        Users savedUser = usersRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateDTO) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, userUpdateDTO);
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }

        if (userUpdateDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(userUpdateDTO.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));
            user.setDepartment(department);
        }

        if (userUpdateDTO.getRoleName() != null) {
            Roles role = rolesRepository.findById(userUpdateDTO.getRoleName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            user.setRole(role);
        }

        Users savedUser = usersRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void changeStatus(UUID id, boolean isActive) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setActive(isActive);
        usersRepository.save(user);
    }
}
