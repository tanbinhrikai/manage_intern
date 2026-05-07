package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.criteria_group.CriteriaGroupCreationRequest;
import com.rikai.backend.dto.request.criteria_group.CriteriaGroupUpdateRequest;
import com.rikai.backend.dto.response.criteria_group.CriteriaGroupResponse;
import com.rikai.backend.service.criteriagroup.ICriteriaGroupService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/criteria-groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CriteriaGroupController {

    ICriteriaGroupService criteriaGroupService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<CriteriaGroupResponse>> getAllGroups() {
        return ApiResponse.buildSuccessResponse(
                criteriaGroupService.getAllGroups(),
                SuccessCode.GET_CRITERIA_GROUP_SUCCESSFUL
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<CriteriaGroupResponse> getGroupById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(
                criteriaGroupService.getGroupById(id),
                SuccessCode.GET_CRITERIA_GROUP_SUCCESSFUL
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CriteriaGroupResponse> createGroup(
            @Valid @RequestBody CriteriaGroupCreationRequest request) {
        return ApiResponse.buildSuccessResponse(
                criteriaGroupService.createGroup(request),
                SuccessCode.CREATE_CRITERIA_GROUP_SUCCESSFUL
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CriteriaGroupResponse> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody CriteriaGroupUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(
                criteriaGroupService.updateGroup(id, request),
                SuccessCode.UPDATE_CRITERIA_GROUP_SUCCESSFUL
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteGroup(@PathVariable Long id) {
        criteriaGroupService.deleteGroup(id);
        return ApiResponse.buildSuccessResponse(
                null,
                SuccessCode.DELETE_CRITERIA_GROUP_SUCCESSFUL
        );
    }
}