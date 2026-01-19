package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.InternStatus;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.InternCreationRequest;
import com.rikai.backend.dto.request.InternUpdateRequest;
import com.rikai.backend.dto.response.InternAnalysisResponse;
import com.rikai.backend.dto.response.InternResponse;
import com.rikai.backend.service.intern.IInternService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/interns")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternController {
    IInternService internService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getAllInterns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "position_id") Long positionId,
            @RequestParam(required = false, name = "mentor_id") UUID mentorId) {
        PageRequest pageable = PageRequest.of(page, limit);
        return ApiResponse.buildSuccessResponse(
                internService.getAllInterns(pageable, keyword, status, positionId, mentorId),
                SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping({"/my-intern", "/my-interns"})
    @PreAuthorize("hasRole('MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getMyIntern(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, limit);
        return ApiResponse.buildSuccessResponse(internService.getMyIntern(pageable, keyword),
                SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping("/not-evaluated-this-week")
    @PreAuthorize("hasRole('MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getInternsNotEvaluatedThisWeek(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return ApiResponse.buildSuccessResponse(
                internService.getInternsNotEvaluatedThisWeek(pageable),
                SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<InternAnalysisResponse> getAnalysis() {
        return ApiResponse.buildSuccessResponse(internService.getAnalysis(), SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getInternsByMentor(@PathVariable UUID mentorId,
                                                                        Pageable pageable) {
        return ApiResponse.buildSuccessResponse(internService.getInternsByMentor(mentorId, pageable),
                SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getInternsByStatus(@PathVariable InternStatus status,
                                                                        Pageable pageable) {
        return ApiResponse.buildSuccessResponse(internService.getInternsByStatus(status, pageable),
                SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping("/position/{positionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<InternResponse>> getInternsByPositionId(@PathVariable Long positionId,
                                                                            Pageable pageable) {
        return ApiResponse.buildSuccessResponse(internService.getInternsByPositionId(positionId, pageable),
                SuccessCode.GET_ALL_INTERNS_SUCCESSFUL);
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<InternResponse> getInternById(@PathVariable Long id) {
        return ApiResponse.buildSuccessResponse(internService.getInternById(id), SuccessCode.GET_INTERN_SUCCESSFUL);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<InternResponse> createIntern(@Valid @RequestBody InternCreationRequest request) {
        return ApiResponse.buildSuccessResponse(internService.createIntern(request),
                SuccessCode.CREATE_INTERN_SUCCESSFUL);
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<InternResponse> updateIntern(@PathVariable Long id,
                                                    @Valid @RequestBody InternUpdateRequest request) {
        return ApiResponse.buildSuccessResponse(internService.updateIntern(id, request),
                SuccessCode.UPDATE_INTERN_SUCCESSFUL);
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<Void> deleteIntern(@PathVariable Long id) {
        internService.deleteIntern(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_INTERN_SUCCESSFUL);
    }
}
