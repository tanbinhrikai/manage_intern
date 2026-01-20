package com.rikai.backend.service.user;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.user.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.UserMapper;
import com.rikai.backend.model.Department;
import com.rikai.backend.model.Enum.RoleType;
import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import com.rikai.backend.repository.DepartmentRepository;
import com.rikai.backend.repository.RolesRepository;
import com.rikai.backend.repository.UsersRepository;
import com.rikai.backend.service.auth.IAuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
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
    IAuthenticationService authenticationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllMentorUsers(
            PageRequest pageRequest,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive,
            Long departmentId) {
        Page<Users> usersPage = usersRepository.findAllMentorUsers(
                pageRequest,
                keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null,
                startDate,
                endDate,
                isActive,
                departmentId);
        List<UserResponse> userResponses = usersPage.getContent().stream()
                .map(UserResponse::fromUser)
                .toList();
        return PageResponse.<UserResponse>builder()
                .items(userResponses)
                .currentPage(usersPage.getNumber())
                .totalPages(usersPage.getTotalPages())
                .totalItems(usersPage.getTotalElements())
                .pageSize(usersPage.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllHrUsers(
            PageRequest pageRequest,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isActive,
            Long departmentId) {
        Page<Users> usersPage = usersRepository.findAllHRUsers(
                pageRequest,
                keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null,
                startDate,
                endDate,
                isActive,
                departmentId);
        List<UserResponse> userResponses = usersPage.getContent().stream()
                .map(UserResponse::fromUser)
                .toList();
        return PageResponse.<UserResponse>builder()
                .items(userResponses)
                .currentPage(usersPage.getNumber())
                .totalPages(usersPage.getTotalPages())
                .totalItems(usersPage.getTotalElements())
                .pageSize(usersPage.getSize())
                .build();
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest userCreateDTO) {
        if (usersRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Users user = userMapper.toUser(userCreateDTO);
        user.setIsActive(true);
        Roles role = null;
        if(userCreateDTO.getRoleName().equals(RoleType.MENTOR)) {
             role = rolesRepository.findByRoleName("MENTOR")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        }
        if(userCreateDTO.getRoleName().equals(RoleType.HR)) {
            role = rolesRepository.findByRoleName("HR")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        }
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

        Users savedUser = usersRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateSelfUser(UserUpdateRequest userUpdateDTO) {
        Users currentUser = authenticationService.getCurrentUser();
        currentUser.setPasswordHash(passwordEncoder.encode(userUpdateDTO.getPassword()));
        Department department = departmentRepository.findById(userUpdateDTO.getDepartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));
        currentUser.setDepartment(department);
        Users savedUser = usersRepository.save(currentUser);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse toggleStatus(UUID id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setIsActive(!user.getIsActive());
        Users savedUser = usersRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }
}
