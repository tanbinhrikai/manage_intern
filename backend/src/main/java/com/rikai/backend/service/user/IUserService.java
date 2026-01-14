package com.rikai.backend.service.user;

import com.rikai.backend.dto.request.UserCreationRequest;
import com.rikai.backend.dto.request.user.UserCreateDTO;
import com.rikai.backend.dto.request.user.UserUpdateDTO;
import com.rikai.backend.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface IUserService {
    Page<Users> getAllMentorUsers(PageRequest pageRequest);
    Users createUser(UserCreateDTO userCreateDTO);
    Users updateUser(UUID id, UserUpdateDTO userUpdateDTO);

    void changeStatus(UUID id, boolean isActive);
}
