package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.department.DepartmentCreationRequest;
import com.rikai.backend.dto.request.department.DepartmentUpdateRequest;
import com.rikai.backend.dto.response.department.DepartmentResponse;
import com.rikai.backend.service.department.IDepartmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentController {
    IDepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<DepartmentResponse>> getAllDepartmentsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, name = "keyword") String keyword
    ) {
        PageRequest pageable = PageRequest.of(page, limit);
        return ApiResponse.buildSuccessResponse(departmentService.getAllDepartmentsList(pageable , keyword),
                SuccessCode.GET_ALL_DEPARTMENTS_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(departmentService.getDepartmentById(id),
                SuccessCode.GET_DEPARTMENT_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentCreationRequest request) {
        return ApiResponse.buildSuccessResponse(departmentService.createDepartment(request),
                SuccessCode.CREATE_DEPARTMENT_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DepartmentResponse> updateDepartment(@PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(departmentService.updateDepartment(id, request),
                SuccessCode.UPDATE_DEPARTMENT_SUCCESSFUL);
    }
}
