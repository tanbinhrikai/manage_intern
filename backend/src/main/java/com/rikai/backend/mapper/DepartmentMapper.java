package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.DepartmentCreationRequest;
import com.rikai.backend.dto.response.DepartmentResponse;
import com.rikai.backend.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "name", target = "title")
    DepartmentResponse toDepartmentResponse(Department department);

    @Mapping(target = "id", ignore = true)
<<<<<<< HEAD
    @Mapping(target = "users", ignore = true)
    Department toDepartment(DepartmentCreationRequest request);
}
