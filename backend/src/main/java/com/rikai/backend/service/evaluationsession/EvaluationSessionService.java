package com.rikai.backend.service.evaluationsession;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.request.evaluationsession.EvaluationSessionCreateRequest;
import com.rikai.backend.dto.request.evaluationsession.EvaluationSessionUpdateRequest;
import com.rikai.backend.dto.response.evaluationsession.EvaluationScoreResponse;
import com.rikai.backend.dto.response.evaluationsession.EvaluationSessionResponse;
import com.rikai.backend.dto.response.evaluationsession.InternEvaluationSummaryResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.Enum.EvaluationConclusion;
import com.rikai.backend.model.Enum.ScoreLabel;
import com.rikai.backend.model.Enum.SessionType;
import com.rikai.backend.model.*;
import com.rikai.backend.repository.*;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EvaluationSessionService implements IEvaluationSessionService {

    EvaluationSessionRepository evaluationSessionRepository;
    EvaluationScoreRepository evaluationScoreRepository;
    InternRepository internRepository;
    WeeklyReportRepository weeklyReportRepository;
    EvaluationCriteriaRepository evaluationCriteriaRepository;
    AuthenticationService authenticationService;

    @Override
    @Transactional
    public EvaluationSessionResponse createSession(EvaluationSessionCreateRequest request) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Intern intern = internRepository.findById(request.getInternId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        // Check authorization
        if (!"ADMIN".equals(currentUser.getRole().getRoleName()) &&
                !intern.getMentor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        // Validate evaluation date is within internship period
        if (request.getEvaluationDate().isBefore(intern.getStartDate()) ||
                request.getEvaluationDate().isAfter(intern.getEndDate())) {
            throw new AppException(ErrorCode.INVALID_EVALUATION_DATE);
        }

        // Check if session already exists for this intern and type
        if (evaluationSessionRepository.existsByIntern_IdAndSessionType(
                request.getInternId(), request.getSessionType())) {
            throw new AppException(ErrorCode.EVALUATION_SESSION_ALREADY_EXISTS);
        }

        // Validate sequential evaluation requirement
        validateSequentialEvaluation(request.getInternId(), request.getSessionType());

        EvaluationSession session = EvaluationSession.builder()
                .intern(intern)
                .mentor(currentUser)
                .sessionType(request.getSessionType())
                .evaluationDate(request.getEvaluationDate())
                .overallComment(request.getOverallComment())
                .build();

        // If scores are provided manually, use them; otherwise auto-generate from weekly reports
        if (request.getScores() != null && !request.getScores().isEmpty()) {
            // Manual scores provided
            Set<EvaluationScore> scores = request.getScores().stream()
                    .map(scoreReq -> {
                        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(scoreReq.getCriteriaId())
                                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
                        return EvaluationScore.builder()
                                .session(session)
                                .criteria(criteria)
                                .score(scoreReq.getScore())
                                .comment(scoreReq.getComment())
                                .build();
                    })
                    .collect(Collectors.toSet());
            session.setScores(scores);
            // Calculate final score from manual scores
            calculateFinalScore(session);
        } else {
            // Auto-generate from weekly reports
            generateScoresFromWeeklyReports(session);
        }

        EvaluationSession savedSession = evaluationSessionRepository.save(session);
        return EvaluationScoreResponse.builder().build().toResponse(savedSession);
    }

    @Override
    @Transactional
    public EvaluationSessionResponse generateSession(Long internId, SessionType sessionType) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        // Check authorization
        if (!"ADMIN".equals(currentUser.getRole().getRoleName()) &&
                !intern.getMentor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        // Check if session already exists
        if (evaluationSessionRepository.existsByIntern_IdAndSessionType(internId, sessionType)) {
            throw new AppException(ErrorCode.EVALUATION_SESSION_ALREADY_EXISTS);
        }

        // Validate sequential evaluation requirement
        validateSequentialEvaluation(internId, sessionType);

        // Calculate evaluation date based on session type
        LocalDate evaluationDate = calculateEvaluationDate(intern, sessionType);

        EvaluationSession session = EvaluationSession.builder()
                .intern(intern)
                .mentor(currentUser)
                .sessionType(sessionType)
                .evaluationDate(evaluationDate)
                .build();

        // Auto-generate scores from weekly reports
        generateScoresFromWeeklyReports(session);

        EvaluationSession savedSession = evaluationSessionRepository.save(session);
        return EvaluationScoreResponse.builder().build().toResponse(savedSession);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationSessionResponse getSessionById(Integer id) {
        EvaluationSession session = evaluationSessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_SESSION_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check authorization
        if (!"ADMIN".equals(currentUser.getRole().getRoleName()) &&
                !session.getIntern().getMentor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        return EvaluationScoreResponse.builder().build().toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EvaluationSessionResponse> getAllSessions(Pageable pageable, Long internId) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Page<EvaluationSession> sessionsPage;
        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            if (internId != null) {
                sessionsPage = evaluationSessionRepository.findByInternId(internId, pageable);
            } else {
                sessionsPage = evaluationSessionRepository.findAll(pageable);
            }
        } else {
            if (internId != null) {
                Intern intern = internRepository.findById(internId)
                        .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));
                if (!intern.getMentor().getId().equals(currentUser.getId())) {
                    throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
                }
                sessionsPage = evaluationSessionRepository.findByInternId(internId, pageable);
            } else {
                sessionsPage = evaluationSessionRepository.findByMentorId(currentUser.getId(), pageable);
            }
        }

        List<EvaluationSessionResponse> responses = sessionsPage.getContent().stream()
                .map(session -> EvaluationScoreResponse.builder().build().toResponse(session))
                .collect(Collectors.toList());

        return PageResponse.<EvaluationSessionResponse>builder()
                .items(responses)
                .currentPage(sessionsPage.getNumber())
                .totalPages(sessionsPage.getTotalPages())
                .totalItems(sessionsPage.getTotalElements())
                .pageSize(sessionsPage.getSize())
                .build();
    }

    @Override
    @Transactional
    public EvaluationSessionResponse updateSession(Integer id, EvaluationSessionUpdateRequest request) {
        EvaluationSession session = evaluationSessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_SESSION_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check authorization
        if (!"ADMIN".equals(currentUser.getRole().getRoleName()) &&
                !session.getIntern().getMentor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        // Update fields
        if (request.getEvaluationDate() != null) {
            if (request.getEvaluationDate().isBefore(session.getIntern().getStartDate()) ||
                    request.getEvaluationDate().isAfter(session.getIntern().getEndDate())) {
                throw new AppException(ErrorCode.INVALID_EVALUATION_DATE);
            }
            session.setEvaluationDate(request.getEvaluationDate());
        }

        if (request.getOverallComment() != null) {
            session.setOverallComment(request.getOverallComment());
        }

        // Update scores if provided
        if (request.getScores() != null && !request.getScores().isEmpty()) {
            // Delete existing scores
            evaluationScoreRepository.deleteBySession_Id(session.getId());

            // Create new scores
            Set<EvaluationScore> scores = request.getScores().stream()
                    .map(scoreReq -> {
                        EvaluationCriteria criteria = evaluationCriteriaRepository.findById(scoreReq.getCriteriaId())
                                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
                        return EvaluationScore.builder()
                                .session(session)
                                .criteria(criteria)
                                .score(scoreReq.getScore())
                                .comment(scoreReq.getComment())
                                .build();
                    })
                    .collect(Collectors.toSet());
            session.setScores(scores);
        }

        // Manual override for final score, level, conclusion
        if (request.getFinalScore() != null) {
            session.setFinalScore(request.getFinalScore());
        }
        if (request.getLevelAssessment() != null) {
            session.setLevelAssessment(request.getLevelAssessment());
        }
        if (request.getConclusion() != null) {
            session.setConclusion(request.getConclusion());
        }

        // Recalculate if scores were updated
        if (request.getScores() != null && !request.getScores().isEmpty()) {
            calculateFinalScore(session);
        }

        EvaluationSession updatedSession = evaluationSessionRepository.save(session);
        return EvaluationScoreResponse.builder().build().toResponse(updatedSession);
    }

    @Override
    @Transactional
    public void deleteSession(Integer id) {
        EvaluationSession session = evaluationSessionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_SESSION_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check authorization
        if (!"ADMIN".equals(currentUser.getRole().getRoleName()) &&
                !session.getIntern().getMentor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        evaluationSessionRepository.delete(session);
    }

    @Override
    @Transactional(readOnly = true)
    public InternEvaluationSummaryResponse getInternEvaluationSummary(Long internId) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Check authorization
        if (!"ADMIN".equals(currentUser.getRole().getRoleName()) &&
                !intern.getMentor().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        // Get all weekly reports
        List<WeeklyReport> weeklyReports = weeklyReportRepository.findByInternIdAndDateRange(
                internId, intern.getStartDate(), intern.getEndDate());

        // Get all evaluation sessions
        List<EvaluationSession> sessions = evaluationSessionRepository.findByInternIdOrderByEvaluationDate(internId);

        // Calculate average weekly score
        BigDecimal averageWeeklyScore = BigDecimal.ZERO;
        if (!weeklyReports.isEmpty()) {
            BigDecimal sum = weeklyReports.stream()
                    .filter(wr -> wr.getAverageScore() != null)
                    .map(WeeklyReport::getAverageScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            averageWeeklyScore = sum.divide(BigDecimal.valueOf(weeklyReports.size()), 2, RoundingMode.HALF_UP);
        }

        // Get sessions by type
        EvaluationSessionResponse firstTerm = null;
        EvaluationSessionResponse midTerm = null;
        EvaluationSessionResponse finalSession = null;

        for (EvaluationSession session : sessions) {
            EvaluationSessionResponse response = EvaluationScoreResponse.builder().build().toResponse(session);
            switch (session.getSessionType()) {
                case FIRST_TERM -> firstTerm = response;
                case MID_TERM -> midTerm = response;
                case FINAL -> finalSession = response;
            }
        }

        // Get overall final score and conclusion
        BigDecimal overallFinalScore = finalSession != null ? finalSession.getFinalScore() : null;
        String overallLevel = finalSession != null ? finalSession.getLevelAssessment() : null;
        String overallConclusion = finalSession != null && finalSession.getConclusion() != null
                ? finalSession.getConclusion().name() : null;

        // Build weekly score trend
        List<InternEvaluationSummaryResponse.WeeklyScoreData> weeklyScoreTrend = weeklyReports.stream()
                .map(wr -> InternEvaluationSummaryResponse.WeeklyScoreData.builder()
                        .weekStartDate(wr.getWeekStartDate())
                        .averageScore(wr.getAverageScore())
                        .build())
                .collect(Collectors.toList());

        return InternEvaluationSummaryResponse.builder()
                .internId(intern.getId())
                .internName(intern.getFullName())
                .startDate(intern.getStartDate())
                .endDate(intern.getEndDate())
                .totalWeeklyReports(weeklyReports.size())
                .averageWeeklyScore(averageWeeklyScore)
                .firstTermSession(firstTerm)
                .midTermSession(midTerm)
                .finalSession(finalSession)
                .overallFinalScore(overallFinalScore)
                .overallLevel(overallLevel)
                .overallConclusion(overallConclusion)
                .weeklyScoreTrend(weeklyScoreTrend)
                .build();
    }

    /**
     * Validate sequential evaluation requirement
     * FIRST_TERM → MID_TERM → FINAL
     */
    private void validateSequentialEvaluation(Long internId, SessionType sessionType) {
        switch (sessionType) {
            case MID_TERM:
                if (!evaluationSessionRepository.existsByIntern_IdAndSessionType(internId, SessionType.FIRST_TERM)) {
                    throw new AppException(ErrorCode.EVALUATION_SESSION_SEQUENCE_INVALID);
                }
                break;
            case FINAL:
                if (!evaluationSessionRepository.existsByIntern_IdAndSessionType(internId, SessionType.MID_TERM)) {
                    throw new AppException(ErrorCode.EVALUATION_SESSION_SEQUENCE_INVALID);
                }
                break;
            case FIRST_TERM:
                // No prerequisite
                break;
        }
    }

    /**
     * Calculate evaluation date based on session type and intern's internship period
     * <p>
     * Logic:
     * - FIRST_TERM: 60 days from start date (day 1 to day 60)
     * - MID_TERM: 60 days starting from the end of FIRST_TERM (day 61 to day 120)
     * - FINAL: Remaining days until end date (day 121 to end)
     */
    private LocalDate calculateEvaluationDate(Intern intern, SessionType sessionType) {
        LocalDate startDate = intern.getStartDate();
        LocalDate endDate = intern.getEndDate();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

        // Each term is approximately 60 days (1/3 of 180 days)
        long daysPerTerm = totalDays / 3;

        return switch (sessionType) {
            case FIRST_TERM -> startDate.plusDays(daysPerTerm); // Day 60 (end of first 60 days)
            case MID_TERM ->
                    startDate.plusDays(daysPerTerm * 2); // Day 120 (end of second 60 days, starting from day 61)
            case FINAL -> endDate; // End of internship (remaining days from day 121)
        };
    }

    /**
     * Generate scores from weekly reports for the evaluation period
     */
    private void generateScoresFromWeeklyReports(EvaluationSession session) {
        Intern intern = session.getIntern();
        LocalDate reportEndDate = session.getEvaluationDate();
        LocalDate reportStartDate;

        switch (session.getSessionType()) {
            case FIRST_TERM:
                // Phase 1: From start date to evaluation date
                reportStartDate = intern.getStartDate();
                break;

            case MID_TERM:
                // Phase 2: From after First Term end date + 1 day to Mid Term evaluation date
                LocalDate firstTermEndDate = calculateEvaluationDate(intern, SessionType.FIRST_TERM);
                reportStartDate = firstTermEndDate.plusDays(1);
                break;

            case FINAL:
                // Phase 3: From after Mid Term end date + 1 day to Final evaluation date
                LocalDate midTermEndDate = calculateEvaluationDate(intern, SessionType.MID_TERM);
                reportStartDate = midTermEndDate.plusDays(1);
                break;
            default:
                throw new AppException(ErrorCode.INVALID_SESSION_TYPE);
        }

        // Logging để debug (khuyên dùng)
        log.info("Fetching reports for {} from {} to {}", session.getSessionType(), reportStartDate, reportEndDate);

        // Get weekly reports in the evaluation period
        List<WeeklyReport> weeklyReports = weeklyReportRepository.findByInternIdAndDateRange(
                intern.getId(), reportStartDate, reportEndDate);

        if (weeklyReports.isEmpty()) {
            // Consideration: Should report the bug or just reset the score to 0?
            // What if mentor too lazy to write a report at this stage?
            throw new AppException(ErrorCode.NO_WEEKLY_REPORTS_FOUND);
        }

        // Get all main criteria
        List<EvaluationCriteria> mainCriteria = evaluationCriteriaRepository.findAllMainCriteria();

        // Calculate average score per main criteria across all weekly reports
        Map<Long, BigDecimal> avgScoreByCriteria = new LinkedHashMap<>();

        for (EvaluationCriteria criteria : mainCriteria) {
            List<BigDecimal> scores = new ArrayList<>();

            for (WeeklyReport report : weeklyReports) {
                if (report.getDetails() != null) {
                    for (WeeklyReportDetail detail : report.getDetails()) {
                        if (detail.getCriteria() != null &&
                                detail.getCriteria().getId().equals(criteria.getId()) &&
                                detail.getScore() != null) {
                            scores.add(detail.getScore());
                        }
                    }
                }
            }

            if (!scores.isEmpty()) {
                BigDecimal sum = scores.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                avgScoreByCriteria.put(criteria.getId(), avg);
            }
        }

        // Create EvaluationScore entities
        Set<EvaluationScore> evaluationScores = avgScoreByCriteria.entrySet().stream()
                .map(entry -> {
                    EvaluationCriteria criteria = evaluationCriteriaRepository.findById(entry.getKey())
                            .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
                    return EvaluationScore.builder()
                            .session(session)
                            .criteria(criteria)
                            .score(entry.getValue())
                            .build();
                })
                .collect(Collectors.toSet());

        session.setScores(evaluationScores);

        // Calculate final score and level
        calculateFinalScore(session);
    }

    /**
     * Calculate final score, level assessment, and conclusion from scores
     */
    private void calculateFinalScore(EvaluationSession session) {
        if (session.getScores() == null || session.getScores().isEmpty()) {
            session.setFinalScore(BigDecimal.ZERO);
            session.setLevelAssessment(ScoreLabel.WEAK.name());
            session.setConclusion(EvaluationConclusion.FAIL);
            return;
        }

        // Calculate average of all main criteria scores
        List<BigDecimal> scores = session.getScores().stream()
                .map(EvaluationScore::getScore)
                .filter(Objects::nonNull)
                .toList();

        if (scores.isEmpty()) {
            session.setFinalScore(BigDecimal.ZERO);
            session.setLevelAssessment(ScoreLabel.WEAK.name());
            session.setConclusion(EvaluationConclusion.FAIL);
            return;
        }

        BigDecimal sum = scores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalScore = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);

        session.setFinalScore(finalScore);

        // Determine level assessment
        ScoreLabel level = ScoreLabel.fromScore(finalScore.intValue());
        session.setLevelAssessment(level.name());

        // Determine conclusion (can be overridden by mentor)
        if (session.getConclusion() == null) {
            if (finalScore.compareTo(BigDecimal.valueOf(7.0)) >= 0) {
                session.setConclusion(EvaluationConclusion.PASS);
            } else if (finalScore.compareTo(BigDecimal.valueOf(5.0)) >= 0) {
                session.setConclusion(EvaluationConclusion.NEED_IMPROVEMENT);
            } else {
                session.setConclusion(EvaluationConclusion.FAIL);
            }
        }
    }
}
