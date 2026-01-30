package com.rikai.backend.service.weeklyreport;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportCreateDTO;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportDetailRequest;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportUpdateDTO;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.weeklyreport.WeeklyReportResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.Enum.StatusWeeklyReport;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Users;
import com.rikai.backend.model.WeeklyReport;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.WeeklyReportRepository;
import com.rikai.backend.repository.EvaluationCriteriaRepository;
import com.rikai.backend.model.EvaluationCriteria;
import com.rikai.backend.event.WeeklyReportCreatedEvent;
import com.rikai.backend.event.WeeklyReportDeletedEvent;
import com.rikai.backend.event.WeeklyReportUpdatedEvent;
import com.rikai.backend.model.WeeklyReportDetail;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeeklyReportService implements IWeeklyReportService {

    WeeklyReportRepository weeklyReportRepository;
    InternRepository internRepository;
    AuthenticationService authenticationService;
    EvaluationCriteriaRepository evaluationCriteriaRepository;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WeeklyReportResponse> getAllReports(PageRequest pageRequest, Long internId) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Page<WeeklyReport> reportsPage;
        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            if (internId != null) {
                reportsPage = weeklyReportRepository.findByInternId(internId, pageRequest);
            } else {
                reportsPage = weeklyReportRepository.findAll(pageRequest);
            }
        } else {
            if (internId != null) {
                Intern intern = internRepository.findById(internId)
                        .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));
                if (!hasAccessToIntern(intern, currentUser)) {
                    throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
                }
                reportsPage = weeklyReportRepository.findByMentorIdAndInternId(
                        currentUser.getId(), internId, pageRequest);
            } else {
                reportsPage = weeklyReportRepository.findByMentorId(currentUser.getId(), pageRequest);
            }
        }

        List<WeeklyReportResponse> responses = reportsPage.getContent().stream()
                .map(WeeklyReportResponse::fromWeeklyReport)
                .collect(Collectors.toList());

        return PageResponse.<WeeklyReportResponse>builder()
                .items(responses)
                .currentPage(reportsPage.getNumber())
                .totalPages(reportsPage.getTotalPages())
                .totalItems(reportsPage.getTotalElements())
                .pageSize(reportsPage.getSize())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportResponse getReportById(Integer id) {
        WeeklyReport report = weeklyReportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WEEKLY_REPORT_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!hasAccessToIntern(report.getIntern(), currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        return WeeklyReportResponse.fromWeeklyReport(report);
    }

    @Override
    @Transactional
    public WeeklyReportResponse createReport(WeeklyReportCreateDTO createDTO) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        Intern intern = internRepository.findById(createDTO.getInternId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        if (!hasAccessToIntern(intern, currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }
        if (weeklyReportRepository.findByInternIdAndWeekStartDate(
                createDTO.getInternId(), createDTO.getWeekStartDate()).isPresent()) {
            throw new AppException(ErrorCode.WEEKLY_REPORT_DUPLICATE);
        }
        Integer weekNumber = calculateWeekNumber(createDTO.getWeekStartDate(), intern.getStartDate());
        WeeklyReport report = WeeklyReport.builder()
                .intern(intern)
                .mentor(currentUser)
                .weekNumber(weekNumber)
                .weekStartDate(createDTO.getWeekStartDate())
                .tasksAssigned(createDTO.getTasksAssigned())
                .tasksCompleted(createDTO.getTasksCompleted())
                .mentorOverallComment(createDTO.getMentorOverallComment())
                .build();

        if (createDTO.getDetails() != null) {
            List<WeeklyReportDetail> details = new ArrayList<>();
            for (WeeklyReportDetailRequest detailReq : createDTO.getDetails()) {
                EvaluationCriteria criteria = evaluationCriteriaRepository.findById(detailReq.getCriteriaId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
                if (criteria.getParent() == null) {
                    // Parent criteria scores are calculated server-side.
                    continue;
                }
                details.add(WeeklyReportDetail.builder()
                        .weeklyReport(report)
                        .criteria(criteria)
                        .score(detailReq.getScore())
                        .comment(detailReq.getComment())
                        .build());
            }
            if (!details.isEmpty()) {
                report.setDetails(details);
                recalculateMainScores(report);
                // Manually trigger averageScore calculation
                report.updateAverageScore();
            }
        }

        WeeklyReport savedReport = weeklyReportRepository.save(report);

        // Publish event để DashboardService có thể bắt được
        eventPublisher.publishEvent(new WeeklyReportCreatedEvent(this, savedReport));

        return WeeklyReportResponse.fromWeeklyReport(savedReport);
    }

    @Override
    @Transactional
    public WeeklyReportResponse updateReport(Integer id, WeeklyReportUpdateDTO updateDTO) {
        // Fetch report with details to ensure averageScore calculation works correctly
        WeeklyReport report = weeklyReportRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.WEEKLY_REPORT_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!hasAccessToIntern(report.getIntern(), currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }
        if (updateDTO.getWeekStartDate() != null) {
            if (!updateDTO.getWeekStartDate().equals(report.getWeekStartDate())) {
                if (weeklyReportRepository.findByInternIdAndWeekStartDate(
                        report.getIntern().getId(), updateDTO.getWeekStartDate()).isPresent()) {
                    throw new AppException(ErrorCode.WEEKLY_REPORT_DUPLICATE);
                }
                report.setWeekStartDate(updateDTO.getWeekStartDate());
                Integer weekNumber = calculateWeekNumber(
                        updateDTO.getWeekStartDate(), report.getIntern().getStartDate());
                report.setWeekNumber(weekNumber);
            }
        }

        if (updateDTO.getTasksAssigned() != null) {
            report.setTasksAssigned(updateDTO.getTasksAssigned());
        }
        if (updateDTO.getTasksCompleted() != null) {
            report.setTasksCompleted(updateDTO.getTasksCompleted());
        }
        if (updateDTO.getDetails() != null) {
            if (report.getDetails() == null) {
                report.setDetails(new ArrayList<>());
            }

            Map<Long, WeeklyReportDetail> existingDetailMap = report.getDetails().stream()
                    .filter(detail -> detail.getCriteria() != null)
                    .collect(Collectors.toMap(
                            detail -> detail.getCriteria().getId(),
                            detail -> detail,
                            (existing, replacement) -> existing));

            Set<Long> requestCriteriaIds = new HashSet<>();
            for (WeeklyReportDetailRequest requestItem : updateDTO.getDetails()) {
                if (requestItem.getCriteriaId() == null) {
                    continue;
                }

                WeeklyReportDetail existingDetail = existingDetailMap.get(requestItem.getCriteriaId());
                if (existingDetail != null) {
                    if (existingDetail.getCriteria() != null && existingDetail.getCriteria().getParent() == null) {
                        // Parent criteria scores are calculated server-side.
                        continue;
                    }
                    existingDetail.setScore(requestItem.getScore());
                    existingDetail.setComment(requestItem.getComment());
                    requestCriteriaIds.add(requestItem.getCriteriaId());
                    continue;
                }

                EvaluationCriteria criteria = evaluationCriteriaRepository
                        .findById(requestItem.getCriteriaId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
                if (criteria.getParent() == null) {
                    // Parent criteria scores are calculated server-side.
                    continue;
                }

                WeeklyReportDetail newDetail = WeeklyReportDetail.builder()
                        .weeklyReport(report)
                        .criteria(criteria)
                        .score(requestItem.getScore())
                        .comment(requestItem.getComment())
                        .build();
                report.getDetails().add(newDetail);
                requestCriteriaIds.add(requestItem.getCriteriaId());
            }

            report.getDetails().removeIf(detail -> detail.getCriteria() != null
                    && detail.getCriteria().getParent() != null
                    && !requestCriteriaIds.contains(detail.getCriteria().getId()));

            recalculateMainScores(report);
        }
        if (updateDTO.getIssuesRisks() != null) {
            report.setIssuesRisks(updateDTO.getIssuesRisks());
        }
        if (updateDTO.getMentorOverallComment() != null) {
            report.setMentorOverallComment(updateDTO.getMentorOverallComment());
        }

        // Manually trigger averageScore calculation to ensure it's updated
        // @PreUpdate might not be triggered if only collection changes
        report.updateAverageScore();

        // Force Hibernate to detect the change by touching a field
        // This ensures @PreUpdate is called
        report.setUpdatedAt(java.time.Instant.now());

        WeeklyReport updatedReport = weeklyReportRepository.save(report);

        // Publish event để DashboardService có thể bắt được
        eventPublisher.publishEvent(new WeeklyReportUpdatedEvent(this, updatedReport));

        return WeeklyReportResponse.fromWeeklyReport(updatedReport);
    }

    /**
     * Delete weekly report by ID
     *
     * @param id
     */

    @Override
    @Transactional
    public void deleteReport(Integer id) {
        WeeklyReport report = weeklyReportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WEEKLY_REPORT_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!hasAccessToIntern(report.getIntern(), currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        // Lưu thông tin trước khi delete để publish event
        Integer reportId = report.getId();
        String internName = report.getIntern().getFullName();
        String mentorName = report.getMentor().getFullName();

        weeklyReportRepository.delete(report);

        // Publish event sau khi delete
        eventPublisher.publishEvent(new WeeklyReportDeletedEvent(this, reportId, internName, mentorName));
    }

    /**
     * Get weekly reports by intern ID with pagination
     *
     * @param internId
     * @param pageRequest
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<WeeklyReportResponse> getReportsByInternId(Long internId, PageRequest pageRequest) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!hasAccessToIntern(intern, currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        Page<WeeklyReport> reports = weeklyReportRepository.findByInternIdOrderByWeekStartDateDesc(internId,
                pageRequest);
        return PageResponse.<WeeklyReportResponse>builder()
                .items(reports.stream()
                        .map(WeeklyReportResponse::fromWeeklyReport)
                        .collect(Collectors.toList()))
                .currentPage(reports.getNumber())
                .totalPages(reports.getTotalPages())
                .totalItems(reports.getTotalElements())
                .pageSize(reports.getSize())
                .build();
    }

    private Integer calculateWeekNumber(LocalDate weekStartDate, LocalDate internStartDate) {
        if (weekStartDate.isBefore(internStartDate)) {
            return 0;
        }
        long daysBetween = ChronoUnit.DAYS.between(internStartDate, weekStartDate);
        return (int) (daysBetween / 7) + 1;
    }

    private boolean hasAccessToIntern(Intern intern, Users currentUser) {
        if (currentUser == null) {
            return false;
        }
        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            return true;
        }
        if ("MENTOR".equals(currentUser.getRole().getRoleName())) {
            return intern.getMentor() != null &&
                    intern.getMentor().getId().equals(currentUser.getId());
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WeeklyReportResponse> getMyReports(PageRequest pageRequest, Long internId, LocalDate startDate,
            LocalDate endDate) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Only mentors can access this endpoint - check if user is a mentor
        if (!"MENTOR".equals(currentUser.getRole().getRoleName())
                && !"ADMIN".equals(currentUser.getRole().getRoleName())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Page<WeeklyReport> reportsPage = weeklyReportRepository.findByMentorIdWithFilters(
                currentUser.getId(),
                internId,
                startDate,
                endDate,
                pageRequest);

        List<WeeklyReportResponse> responses = reportsPage.getContent().stream()
                .map(WeeklyReportResponse::fromWeeklyReport)
                .collect(Collectors.toList());

        return PageResponse.<WeeklyReportResponse>builder()
                .items(responses)
                .currentPage(reportsPage.getNumber())
                .totalPages(reportsPage.getTotalPages())
                .totalItems(reportsPage.getTotalElements())
                .pageSize(reportsPage.getSize())
                .build();
    }

    /**
     * Recalculate main scores based on sub-criteria scores and weights
     *
     * @param report
     */
    private void recalculateMainScores(WeeklyReport report) {
        if (report.getDetails() == null || report.getDetails().isEmpty()) {
            return;
        }

        List<WeeklyReportDetail> details = report.getDetails();
        Set<Long> seenParentIds = new HashSet<>();
        details.removeIf(detail -> {
            if (detail.getCriteria() != null && detail.getCriteria().getParent() == null) {
                Long criteriaId = detail.getCriteria().getId();
                if (criteriaId != null) {
                    if (seenParentIds.contains(criteriaId)) {
                        return true;
                    }
                    seenParentIds.add(criteriaId);
                }
            }
            return false;
        });

        Map<Long, WeeklyReportDetail> parentDetailMap = details.stream()
                .filter(detail -> detail.getCriteria() != null && detail.getCriteria().getParent() == null)
                .collect(Collectors.toMap(
                        detail -> detail.getCriteria().getId(),
                        detail -> detail,
                        (existing, replacement) -> existing));

        // Group sub-criteria by their parent criteria
        Map<EvaluationCriteria, List<WeeklyReportDetail>> subCriteriaByParent = new LinkedHashMap<>();
        for (WeeklyReportDetail detail : details) {
            EvaluationCriteria criteria = detail.getCriteria();
            if (criteria != null && criteria.getParent() != null) {
                subCriteriaByParent
                        .computeIfAbsent(criteria.getParent(), parent -> new ArrayList<>())
                        .add(detail);
            }
        }

        // Loop through each parent criteria to calculate main score
        for (Map.Entry<EvaluationCriteria, List<WeeklyReportDetail>> entry : subCriteriaByParent.entrySet()) {
            EvaluationCriteria parent = entry.getKey();
            List<WeeklyReportDetail> subDetails = entry.getValue();

            BigDecimal weightedSum = BigDecimal.ZERO; // Sum (score * weight)
            BigDecimal totalWeight = BigDecimal.ZERO; // Sum weight

            for (WeeklyReportDetail subDetail : subDetails) {
                if (subDetail.getScore() == null) {
                    continue;
                }
                BigDecimal score = subDetail.getScore();

                BigDecimal weight = subDetail.getCriteria().getWeight() != null
                        ? subDetail.getCriteria().getWeight()
                        : BigDecimal.ONE;

                // Caculate numerator: cumulative (score * weight)
                weightedSum = weightedSum.add(score.multiply(weight));

                // Calculate denominator: cumulative weight
                totalWeight = totalWeight.add(weight);
            }

            // If totalWeight is 0, skip to avoid division by zero
            if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // ecipe: weightedSum / totalWeight
            // setScale(2, RoundingMode.HALF_UP) to round to 2 decimal places
            BigDecimal finalScore = weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);

            // Clamping: ensure finalScore is between 0 and 10
            if (finalScore.compareTo(BigDecimal.ZERO) < 0) {
                finalScore = BigDecimal.ZERO;
            } else if (finalScore.compareTo(BigDecimal.TEN) > 0) {
                finalScore = BigDecimal.TEN;
            }

            WeeklyReportDetail mainDetail = parentDetailMap.get(parent.getId());
            if (mainDetail != null) {
                mainDetail.setScore(finalScore);
            } else {
                WeeklyReportDetail newDetail = WeeklyReportDetail.builder()
                        .weeklyReport(report)
                        .criteria(parent)
                        .score(finalScore)
                        .build();
                details.add(newDetail);
            }
        }

        Set<Long> parentIdsWithSubCriteria = subCriteriaByParent.keySet().stream()
                .map(EvaluationCriteria::getId)
                .collect(Collectors.toSet());
        details.removeIf(detail -> detail.getCriteria() != null
                && detail.getCriteria().getParent() == null
                && !parentIdsWithSubCriteria.contains(detail.getCriteria().getId()));
    }
}
