package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.CriteriaScoreDefinitionCreationRequest;
import com.rikai.backend.dto.request.CriteriaScoreDefinitionUpdateRequest;
import com.rikai.backend.dto.response.CriteriaScoreDefinitionResponse;
import com.rikai.backend.service.criteriascoredefinition.ICriteriaScoreDefinitionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/criteria-score-definitions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CriteriaScoreDefinitionController {
    ICriteriaScoreDefinitionService criteriaScoreDefinitionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<CriteriaScoreDefinitionResponse>> getAllScoreDefinitions() {
        return ApiResponse.buildSuccessResponse(criteriaScoreDefinitionService.getAllScoreDefinitions(),
                SuccessCode.GET_ALL_CRITERIA_SCORE_DEFINITION_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<CriteriaScoreDefinitionResponse> getScoreDefinitionById(@PathVariable Integer id) {
        return ApiResponse.buildSuccessResponse(criteriaScoreDefinitionService.getScoreDefinitionById(id),
                SuccessCode.GET_CRITERIA_SCORE_DEFINITION_SUCCESSFUL);
    }

    @GetMapping("/criteria/{criteriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<CriteriaScoreDefinitionResponse>> getScoreDefinitionsByCriteriaId(
            @PathVariable Integer criteriaId) {
        return ApiResponse.buildSuccessResponse(
                criteriaScoreDefinitionService.getScoreDefinitionsByCriteriaId(criteriaId),
                SuccessCode.GET_ALL_CRITERIA_SCORE_DEFINITION_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CriteriaScoreDefinitionResponse> createScoreDefinition(
            @Valid @RequestBody CriteriaScoreDefinitionCreationRequest request) {
        return ApiResponse.buildSuccessResponse(criteriaScoreDefinitionService.createScoreDefinition(request),
                SuccessCode.CREATE_CRITERIA_SCORE_DEFINITION_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CriteriaScoreDefinitionResponse> updateScoreDefinition(@PathVariable Integer id,
            @Valid @RequestBody CriteriaScoreDefinitionUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(criteriaScoreDefinitionService.updateScoreDefinition(id, request),
                SuccessCode.UPDATE_CRITERIA_SCORE_DEFINITION_SUCCESSFUL);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteScoreDefinition(@PathVariable Integer id) {
        criteriaScoreDefinitionService.deleteScoreDefinition(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_CRITERIA_SCORE_DEFINITION_SUCCESSFUL);
    }
}
