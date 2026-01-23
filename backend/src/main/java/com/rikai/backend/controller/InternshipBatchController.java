package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.batch.InternshipBatchCreationRequest;
import com.rikai.backend.dto.request.batch.InternshipBatchUpdateRequest;
import com.rikai.backend.dto.response.batch.InternshipBatchResponse;
import com.rikai.backend.model.Enum.BatchStatus;
import com.rikai.backend.service.internship_batch.IInternshipBatchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internship-batches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternshipBatchController {
    IInternshipBatchService internshipBatchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternshipBatchResponse>> getAllBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BatchStatus status) {
        PageRequest pageable = PageRequest.of(page, limit);
        return ApiResponse.buildSuccessResponse(
                internshipBatchService.getAllBatches(pageable, keyword, status),
                SuccessCode.GET_ALL_BATCHES_SUCCESSFUL);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<InternshipBatchResponse> getBatchById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(
                internshipBatchService.getBatchById(id),
                SuccessCode.GET_BATCH_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InternshipBatchResponse> createBatch(
            @Valid @RequestBody InternshipBatchCreationRequest request) {
        return ApiResponse.buildSuccessResponse(
                internshipBatchService.createBatch(request),
                SuccessCode.CREATE_BATCH_SUCCESSFUL);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InternshipBatchResponse> updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody InternshipBatchUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(
                internshipBatchService.updateBatch(id, request),
                SuccessCode.UPDATE_BATCH_SUCCESSFUL);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBatch(@PathVariable Long id) {
        internshipBatchService.deleteBatch(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_BATCH_SUCCESSFUL);
    }
}
