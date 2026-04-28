package com.rikai.backend.service.department;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.department.DepartmentCreationRequest;
import com.rikai.backend.dto.request.department.DepartmentUpdateRequest;
import com.rikai.backend.dto.response.department.DepartmentResponse;
import org.springframework.data.domain.Pageable;

public interface IDepartmentService {

    PageResponse<DepartmentResponse> getAllDepartmentsList(Pageable pageable , String keyword);

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse createDepartment(DepartmentCreationRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request);
}
