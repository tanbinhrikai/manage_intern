package com.rikai.backend.service.department;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.DepartmentCreationRequest;
import com.rikai.backend.dto.request.DepartmentUpdateRequest;
import com.rikai.backend.dto.response.DepartmentResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.mapper.DepartmentMapper;
import com.rikai.backend.model.Department;
import com.rikai.backend.repository.DepartmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentService implements IDepartmentService {
    DepartmentRepository departmentRepository;
    DepartmentMapper departmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartmentsList() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toDepartmentResponse)
                .collect(Collectors.toList());
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

//        department.setTitle(request.getTitle());

        Department saved = departmentRepository.save(department);
        return departmentMapper.toDepartmentResponse(saved);
    }
}
