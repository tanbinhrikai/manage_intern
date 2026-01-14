package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.UserCreationRequest;
import com.rikai.backend.dto.request.UserUpdateRequest;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { DepartmentMapper.class })
public interface UserMapper {

    @Mapping(target = "isActive", source = "active")
    UserResponse toUserResponse(Users users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)

    @Mapping(target = "department", ignore = true)
    Users toUser(UserCreationRequest userCreationRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)

    @Mapping(target = "department", ignore = true)
    Users toUser(UserUpdateRequest userUpdateRequest);

}
