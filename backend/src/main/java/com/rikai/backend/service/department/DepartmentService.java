package com.rikai.backend.service.department;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.department.DepartmentCreationRequest;
import com.rikai.backend.dto.request.department.DepartmentUpdateRequest;
import com.rikai.backend.dto.response.department.DepartmentResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.DepartmentMapper;
import com.rikai.backend.model.Department;
import com.rikai.backend.repository.DepartmentRepository;
import com.rikai.backend.repository.UsersRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentService implements IDepartmentService {
    DepartmentRepository departmentRepository;
    DepartmentMapper departmentMapper;
    UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> getAllDepartmentsList(Pageable pageable , String keyword) {
        String keywordValue = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<Department> departments = departmentRepository.getAllDepartmentByKeyword(pageable , keywordValue);

        return PageResponse.fromPage(departments.map(dept -> {
            DepartmentResponse response = departmentMapper.toDepartmentResponse(dept);
            response.setMentorCount(usersRepository.countByDepartment_IdAndRole_RoleName(dept.getId(), "MENTOR"));
            return response;
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));
        return departmentMapper.toDepartmentResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentCreationRequest request) {
        Department department = departmentMapper.toDepartment(request);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        department.setTitle(request.getTitle());

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(saved);
    }
}
