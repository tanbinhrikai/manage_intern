package com.rikai.backend.service.evaluationsession;

import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.evaluationsession.EvaluationSessionCreateRequest;
import com.rikai.backend.dto.request.evaluationsession.EvaluationSessionUpdateRequest;
import com.rikai.backend.dto.response.evaluationsession.EvaluationSessionResponse;
import com.rikai.backend.dto.response.evaluationsession.InternEvaluationSummaryResponse;
import com.rikai.backend.model.Enum.SessionType;
import org.springframework.data.domain.Pageable;

public interface IEvaluationSessionService {
    /**
     * Create evaluation session (auto-generate from weekly reports or manual)
     */
    EvaluationSessionResponse createSession(EvaluationSessionCreateRequest request);

    /**
     * Auto-generate evaluation session from weekly reports
     */
    EvaluationSessionResponse generateSession(Long internId, SessionType sessionType);

    /**
     * Get evaluation session by ID
     */
    EvaluationSessionResponse getSessionById(Integer id);

    /**
     * Get all evaluation sessions with pagination
     */
    PageResponse<EvaluationSessionResponse> getAllSessions(Pageable pageable, Long internId);

    /**
     * Update evaluation session (allow override scores and conclusion)
     */
    EvaluationSessionResponse updateSession(Integer id, EvaluationSessionUpdateRequest request);

    /**
     * Delete evaluation session
     */
    void deleteSession(Integer id);

    /**
     * Get evaluation summary for an intern
     */
    InternEvaluationSummaryResponse getInternEvaluationSummary(Long internId);
}
