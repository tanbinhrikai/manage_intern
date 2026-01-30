package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.learning_plan.LearningPlanCreationRequest;
import com.rikai.backend.dto.request.learning_plan.LearningPlanUpdateRequest;
import com.rikai.backend.dto.response.learning_plan.LearningPlanResponse;
import com.rikai.backend.service.learningplan.ILearningPlanService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning-plans")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LearningPlanController {
    ILearningPlanService learningPlanService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<LearningPlanResponse>> getAllLearningPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "intern_id") Long internId) {
        PageRequest pageable = PageRequest.of(page, limit);
        return ApiResponse.buildSuccessResponse(
                learningPlanService.getAllLearningPlans(pageable, keyword, internId),
                SuccessCode.GET_ALL_LEARNING_PLANS_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<LearningPlanResponse> getLearningPlanById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(
                learningPlanService.getLearningPlanById(id),
                SuccessCode.GET_LEARNING_PLAN_SUCCESSFUL);
    }

    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<LearningPlanResponse>> getLearningPlansByInternId(@PathVariable Long internId) {
        return ApiResponse.buildSuccessResponse(
                learningPlanService.getLearningPlansByInternId(internId),
                SuccessCode.GET_ALL_LEARNING_PLANS_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<LearningPlanResponse> createLearningPlan(
            @Valid @RequestBody LearningPlanCreationRequest request) {
        return ApiResponse.buildSuccessResponse(
                learningPlanService.createLearningPlan(request),
                SuccessCode.CREATE_LEARNING_PLAN_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<LearningPlanResponse> updateLearningPlan(
            @PathVariable Long id,
            @Valid @RequestBody LearningPlanUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(
                learningPlanService.updateLearningPlan(id, request),
                SuccessCode.UPDATE_LEARNING_PLAN_SUCCESSFUL);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteLearningPlan(@PathVariable Long id) {
        learningPlanService.deleteLearningPlan(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_LEARNING_PLAN_SUCCESSFUL);
    }
}
