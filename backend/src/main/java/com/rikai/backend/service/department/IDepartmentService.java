package com.rikai.backend.service.department;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.DepartmentCreationRequest;
import com.rikai.backend.dto.request.DepartmentUpdateRequest;
import com.rikai.backend.dto.response.DepartmentResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDepartmentService {

    List<DepartmentResponse> getAllDepartmentsList();

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse createDepartment(DepartmentCreationRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request);
}
