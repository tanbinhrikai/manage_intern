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
import com.rikai.backend.model.WeeklyReportDetail;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeeklyReportService implements IWeeklyReportService {

    WeeklyReportRepository weeklyReportRepository;
    InternRepository internRepository;
    AuthenticationService authenticationService;
    EvaluationCriteriaRepository evaluationCriteriaRepository;

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
            List<WeeklyReportDetail> details = createDTO.getDetails().stream().map(detailReq -> {
                EvaluationCriteria criteria = evaluationCriteriaRepository.findById(detailReq.getCriteriaId())
                        .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));
                return WeeklyReportDetail.builder()
                        .weeklyReport(report)
                        .criteria(criteria)
                        .score(detailReq.getScore())
                        .comment(detailReq.getComment())
                        .build();
            }).collect(Collectors.toList());
            report.setDetails(details);
            recalculateMainScores(report);
        }

        WeeklyReport savedReport = weeklyReportRepository.save(report);
        return WeeklyReportResponse.fromWeeklyReport(savedReport);
    }

    @Override
    @Transactional
    public WeeklyReportResponse updateReport(Integer id, WeeklyReportUpdateDTO updateDTO) {
        WeeklyReport report = weeklyReportRepository.findById(id)
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

            List<Long> requestCriteriaIds = updateDTO.getDetails().stream()
                    .map(WeeklyReportDetailRequest::getCriteriaId).toList();

            report.getDetails().removeIf(
                    weeklyReportDetail -> !requestCriteriaIds.contains(weeklyReportDetail.getCriteria().getId()));

            for (WeeklyReportDetailRequest weeklyReportDetailRequest : updateDTO.getDetails()) {
                WeeklyReportDetail existingDetail = report.getDetails().stream()
                        .filter(detail -> detail.getCriteria().getId()
                                .equals(weeklyReportDetailRequest.getCriteriaId()))
                        .findFirst()
                        .orElse(null);

                if (existingDetail != null) {
                    existingDetail.setScore(weeklyReportDetailRequest.getScore());
                    existingDetail.setComment(weeklyReportDetailRequest.getComment());
                } else {
                    EvaluationCriteria criteria = evaluationCriteriaRepository
                            .findById(weeklyReportDetailRequest.getCriteriaId())
                            .orElseThrow(() -> new AppException(ErrorCode.EVALUATION_CRITERIA_NOT_EXISTED));

                    WeeklyReportDetail newDetail = WeeklyReportDetail.builder()
                            .weeklyReport(report)
                            .criteria(criteria)
                            .score(weeklyReportDetailRequest.getScore())
                            .comment(weeklyReportDetailRequest.getComment())
                            .build();
                    report.getDetails().add(newDetail);
                }
            }
            recalculateMainScores(report);
        }
        if (updateDTO.getIssuesRisks() != null) {
            report.setIssuesRisks(updateDTO.getIssuesRisks());
        }
        if (updateDTO.getMentorOverallComment() != null) {
            report.setMentorOverallComment(updateDTO.getMentorOverallComment());
        }

        WeeklyReport updatedReport = weeklyReportRepository.save(report);
        return WeeklyReportResponse.fromWeeklyReport(updatedReport);
    }

    /**
     * Delete weekly report by ID
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

        weeklyReportRepository.delete(report);
    }

    /**
     * Get weekly reports by intern ID with pagination
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

        Page<WeeklyReport> reports = weeklyReportRepository.findByInternIdOrderByWeekStartDateDesc(internId, pageRequest);
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

    /**
     * Recalculate main scores based on sub-criteria scores and weights
     * @param report
     */
    private void recalculateMainScores(WeeklyReport report) {
        if (report.getDetails() == null || report.getDetails().isEmpty()) {
            return;
        }

        List<WeeklyReportDetail> details = report.getDetails();
        // Delete parent criteria details to recalculate
        details.removeIf(detail -> detail.getCriteria() != null && detail.getCriteria().getParent() == null);

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

            WeeklyReportDetail mainDetail = WeeklyReportDetail.builder()
                    .weeklyReport(report)
                    .criteria(parent)
                    .score(finalScore)
                    .build();
            details.add(mainDetail);
        }
    }
}
