package com.rikai.backend.mapper;

import com.rikai.backend.dto.request.department.DepartmentCreationRequest;
import com.rikai.backend.dto.response.department.DepartmentResponse;
import com.rikai.backend.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    DepartmentResponse toDepartmentResponse(Department department);

    @Mapping(target = "id", ignore = true)

    @Mapping(target = "users", ignore = true)
    Department toDepartment(DepartmentCreationRequest request);
}
