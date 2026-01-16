package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.EvaluationCriteriaResponse;
import com.rikai.backend.service.evaluationcriteria.IEvaluationCriteriaService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluation-criteria")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationCriteriaController {
    IEvaluationCriteriaService evaluationCriteriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<EvaluationCriteriaResponse>> getAllEvaluationCriteria() {
        return ApiResponse.buildSuccessResponse(evaluationCriteriaService.getAllEvaluationCriteria(),
                SuccessCode.GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<EvaluationCriteriaResponse> getEvaluationCriteriaById(@PathVariable Integer id) {
        return ApiResponse.buildSuccessResponse(evaluationCriteriaService.getEvaluationCriteriaById(id),
                SuccessCode.GET_EVALUATION_CRITERIA_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EvaluationCriteriaResponse> createEvaluationCriteria(
            @Valid @RequestBody EvaluationCriteriaCreationRequest request) {
        return ApiResponse.buildSuccessResponse(evaluationCriteriaService.createEvaluationCriteria(request),
                SuccessCode.CREATE_EVALUATION_CRITERIA_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EvaluationCriteriaResponse> updateEvaluationCriteria(@PathVariable Integer id,
            @Valid @RequestBody EvaluationCriteriaUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(evaluationCriteriaService.updateEvaluationCriteria(id, request),
                SuccessCode.UPDATE_EVALUATION_CRITERIA_SUCCESSFUL);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteEvaluationCriteria(@PathVariable Integer id) {
        evaluationCriteriaService.deleteEvaluationCriteria(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_EVALUATION_CRITERIA_SUCCESSFUL);
    }
}
