package com.rikai.backend.service.user;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.user.UserCreateDTO;
import com.rikai.backend.dto.request.user.UserUpdateDTO;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements IUserService {
    UsersRepository usersRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    private final RolesRepository rolesRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Users> getAllMentorUsers(PageRequest pageRequest) {
        return usersRepository.findAllMentorUsers(pageRequest);
    }

    @Override
    @Transactional
    public Users createUser(UserCreateDTO userCreateDTO) {
        if (usersRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Users user = Users.builder()
                .email(userCreateDTO.getEmail())
                .passwordHash(userCreateDTO.getPassword())
                .fullName(userCreateDTO.getFullName())
                .dateOfBirth(userCreateDTO.getDateOfBirth())
                .build();
        Optional<Roles> role = rolesRepository.findByRoleName("MENTOR");
        user.setPasswordHash(passwordEncoder.encode(userCreateDTO.getPassword()));
        role.ifPresent(user::setRole);
        return usersRepository.save(user);
    }

    @Override
    public Users updateUser(UUID id, UserUpdateDTO userUpdateDTO) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getFullName() != null) {
            user.setFullName(userUpdateDTO.getFullName());
        }
        if (userUpdateDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(userUpdateDTO.getDateOfBirth());
        }
        if (userUpdateDTO.getPassword() != null && !userUpdateDTO.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }
        Department department = departmentRepository.findById(userUpdateDTO.getDepartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));
        user.setDepartment(department);
        return usersRepository.save(user);
    }

    @Override
    public void changeStatus(UUID id, boolean isActive) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setActive(isActive);
        usersRepository.save(user);
    }
}
