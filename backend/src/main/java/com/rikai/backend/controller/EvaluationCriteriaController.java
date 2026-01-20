package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaCreationRequest;
import com.rikai.backend.dto.request.evaluation_criteria.EvaluationCriteriaUpdateRequest;
import com.rikai.backend.dto.response.criteria.CriteriaCategoryResponse;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.dto.response.score_label.ScoreLabelResponse;
import com.rikai.backend.model.Enum.CriteriaCategory;
import com.rikai.backend.service.evaluationcriteria.IEvaluationCriteriaService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluation-criteria")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationCriteriaController {

    IEvaluationCriteriaService evaluationCriteriaService;

    /**
     * Get all evaluation criteria in hierarchy
     */
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<CriteriaCategoryResponse>> getAllCriteriaHierarchy() {
        log.info("REST request to get all evaluation criteria in hierarchy");
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getAllCriteriaHierarchy(),
                SuccessCode.GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL
        );
    }

    /**
     * Get all main criteria (without parent)
     */
    @GetMapping("/main")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<EvaluationCriteriaResponse>> getAllMainCriteria() {
        log.info("REST request to get all main criteria");
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getAllMainCriteria(),
                SuccessCode.GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL
        );
    }

    /**
     * Get all sub-criteria (can be scored)
     */
    @GetMapping("/sub")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<EvaluationCriteriaResponse>> getAllSubCriteria() {
        log.info("REST request to get all sub-criteria");
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getAllSubCriteria(),
                SuccessCode.GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL
        );
    }

    /**
     * Get sub-criteria by parent id
     */
    @GetMapping("/{parentId}/sub-criteria")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<EvaluationCriteriaResponse>> getSubCriteriaByParentId(
            @PathVariable Long parentId) {
        log.info("REST request to get sub-criteria by parent id: {}", parentId);
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getSubCriteriaByParentId(parentId),
                SuccessCode.GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL
        );
    }

    /**
     * Get criteria by id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<EvaluationCriteriaResponse> getCriteriaById(@PathVariable Long id) {
        log.info("REST request to get evaluation criteria by id: {}", id);
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getCriteriaById(id),
                SuccessCode.GET_EVALUATION_CRITERIA_SUCCESSFUL
        );
    }

    /**
     * Get criteria by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<EvaluationCriteriaResponse>> getCriteriaByCategory(
            @PathVariable CriteriaCategory category) {
        log.info("REST request to get evaluation criteria by category: {}", category);
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getCriteriaByCategory(category),
                SuccessCode.GET_ALL_EVALUATION_CRITERIA_SUCCESSFUL
        );
    }

    /**
     * Get all categories
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<CriteriaCategoryResponse>> getAllCategories() {
        log.info("REST request to get all categories");
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getAllCategories(),
                SuccessCode.GET_ALL_CATEGORIES_SUCCESSFUL
        );
    }

    /**
     * Get all score labels
     */
    @GetMapping("/score-labels")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<ScoreLabelResponse>> getAllScoreLabels() {
        log.info("REST request to get all score labels");
        return ApiResponse.buildSuccessResponse(
                evaluationCriteriaService.getAllScoreLabels(),
                SuccessCode.GET_ALL_CRITERIA_SCORE_DEFINITION_SUCCESSFUL
        );
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
    public ApiResponse<EvaluationCriteriaResponse> updateEvaluationCriteria(@PathVariable Long id,
                                                                            @Valid @RequestBody EvaluationCriteriaUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(evaluationCriteriaService.updateEvaluationCriteria(id, request),
                SuccessCode.UPDATE_EVALUATION_CRITERIA_SUCCESSFUL);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteEvaluationCriteria(@PathVariable Long id) {
        evaluationCriteriaService.deleteEvaluationCriteria(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_EVALUATION_CRITERIA_SUCCESSFUL);
    }


}
