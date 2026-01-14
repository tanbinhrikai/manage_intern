package com.rikai.backend.service.weeklyreport;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportCreateDTO;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportUpdateDTO;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.weeklyreport.WeeklyReportResponse;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.Users;
import com.rikai.backend.model.WeeklyReport;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.WeeklyReportRepository;
import com.rikai.backend.service.auth.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeeklyReportService implements IWeeklyReportService {

    WeeklyReportRepository weeklyReportRepository;
    InternRepository internRepository;
    AuthenticationService authenticationService;

    /**
     * Calculate week number from intern start_date
     */
    private Integer calculateWeekNumber(LocalDate weekStartDate, LocalDate internStartDate) {
        if (weekStartDate.isBefore(internStartDate)) {
            return 0;
        }
        long daysBetween = ChronoUnit.DAYS.between(internStartDate, weekStartDate);
        return (int) (daysBetween / 7) + 1;
    }

    /**
     * Check if current user is admin or mentor of the intern
     */
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
    public PageResponse<WeeklyReportResponse> getAllReports(Pageable pageable, Long internId) {
        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Page<WeeklyReport> reportsPage;
        if ("ADMIN".equals(currentUser.getRole().getRoleName())) {
            if (internId != null) {
                reportsPage = weeklyReportRepository.findByInternId(internId, pageable);
            } else {
                reportsPage = weeklyReportRepository.findAll(pageable);
            }
        } else {
            if (internId != null) {
                Intern intern = internRepository.findById(internId)
                        .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));
                if (!hasAccessToIntern(intern, currentUser)) {
                    throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
                }
                reportsPage = weeklyReportRepository.findByMentorIdAndInternId(
                        currentUser.getId(), internId, pageable);
            } else {
                reportsPage = weeklyReportRepository.findByMentorId(currentUser.getId(), pageable);
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
                .outputQuality(createDTO.getOutputQuality())
                .proactivityScore(createDTO.getProactivityScore())
                .progressScore(createDTO.getProgressScore())
                .issuesRisks(createDTO.getIssuesRisks())
                .mentorOverallComment(createDTO.getMentorOverallComment())
                .status("submitted")
                .build();

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
                // Recalculate week_number
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
        if (updateDTO.getOutputQuality() != null) {
            report.setOutputQuality(updateDTO.getOutputQuality());
        }
        if (updateDTO.getProactivityScore() != null) {
            report.setProactivityScore(updateDTO.getProactivityScore());
        }
        if (updateDTO.getProgressScore() != null) {
            report.setProgressScore(updateDTO.getProgressScore());
        }
        if (updateDTO.getIssuesRisks() != null) {
            report.setIssuesRisks(updateDTO.getIssuesRisks());
        }
        if (updateDTO.getMentorOverallComment() != null) {
            report.setMentorOverallComment(updateDTO.getMentorOverallComment());
        }
        if (updateDTO.getStatus() != null) {
            report.setStatus(updateDTO.getStatus());
        }

        WeeklyReport updatedReport = weeklyReportRepository.save(report);
        return WeeklyReportResponse.fromWeeklyReport(updatedReport);
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyReportResponse> getReportsByInternId(Long internId) {
        Intern intern = internRepository.findById(internId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERN_NOT_EXISTED));

        Users currentUser = authenticationService.getCurrentUser();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!hasAccessToIntern(intern, currentUser)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_INTERN_ACCESS);
        }

        List<WeeklyReport> reports = weeklyReportRepository.findByInternIdOrderByWeekStartDateDesc(internId);
        return reports.stream()
                .map(WeeklyReportResponse::fromWeeklyReport)
                .collect(Collectors.toList());
    }
}
