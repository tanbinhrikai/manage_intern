package com.rikai.backend.controller;


import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.model.Department;
import com.rikai.backend.service.department.IDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final IDepartmentService departmentService;

    @GetMapping("")
    public ApiResponse<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ApiResponse.buildSuccessResponse(
                departments,
                SuccessCode.GET_ALL_DEPARTMENTS_SUCCESSFUL
        );
    }
}
