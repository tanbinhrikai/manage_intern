package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.evaluationsession.EvaluationSessionCreateRequest;
import com.rikai.backend.dto.request.evaluationsession.EvaluationSessionUpdateRequest;
import com.rikai.backend.dto.response.evaluationsession.EvaluationSessionResponse;
import com.rikai.backend.dto.response.evaluationsession.InternEvaluationSummaryResponse;
import com.rikai.backend.model.Enum.SessionType;
import com.rikai.backend.service.evaluationsession.IEvaluationSessionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/evaluation-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationSessionController {

    IEvaluationSessionService evaluationSessionService;

    /**
     * POST /evaluation-sessions
     * Create evaluation session (manual or auto-generate)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<EvaluationSessionResponse> createSession(
            @Valid @RequestBody EvaluationSessionCreateRequest request) {
        EvaluationSessionResponse response = evaluationSessionService.createSession(request);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.CREATE_EVALUATION_SESSION_SUCCESSFUL);
    }

    /**
     * POST /evaluation-sessions/generate
     * Auto-generate evaluation session from weekly reports
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<EvaluationSessionResponse> generateSession(
            @RequestParam Long internId,
            @RequestParam SessionType sessionType) {
        EvaluationSessionResponse response = evaluationSessionService.generateSession(internId, sessionType);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.GENERATE_EVALUATION_SESSION_SUCCESSFUL);
    }

    /**
     * GET /evaluation-sessions
     * Get all evaluation sessions with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<EvaluationSessionResponse>> getAllSessions(
            @RequestParam(required = false) Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        PageResponse<EvaluationSessionResponse> response = evaluationSessionService.getAllSessions(pageable, internId);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.GET_ALL_EVALUATION_SESSIONS_SUCCESSFUL);
    }

    /**
     * GET /evaluation-sessions/{id}
     * Get evaluation session by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<EvaluationSessionResponse> getSessionById(@PathVariable Integer id) {
        EvaluationSessionResponse response = evaluationSessionService.getSessionById(id);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.GET_EVALUATION_SESSION_SUCCESSFUL);
    }

    /**
     * PUT /evaluation-sessions/{id}
     * Update evaluation session (allow override scores and conclusion)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<EvaluationSessionResponse> updateSession(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluationSessionUpdateRequest request) {
        EvaluationSessionResponse response = evaluationSessionService.updateSession(id, request);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.UPDATE_EVALUATION_SESSION_SUCCESSFUL);
    }

    /**
     * DELETE /evaluation-sessions/{id}
     * Delete evaluation session
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<Void> deleteSession(@PathVariable Integer id) {
        evaluationSessionService.deleteSession(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_EVALUATION_SESSION_SUCCESSFUL);
    }

    /**
     * GET /interns/{internId}/evaluation-summary
     * Get evaluation summary for an intern
     */
    @GetMapping("/interns/{internId}/evaluation-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<InternEvaluationSummaryResponse> getInternEvaluationSummary(
            @PathVariable Long internId) {
        InternEvaluationSummaryResponse response = evaluationSessionService.getInternEvaluationSummary(internId);
        return ApiResponse.buildSuccessResponse(response, SuccessCode.GET_INTERN_EVALUATION_SUMMARY_SUCCESSFUL);
    }
}
