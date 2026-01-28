package com.rikai.backend.service.dashboard;

import com.rikai.backend.common.InternStatus;
import com.rikai.backend.dto.response.dashboard.ActivityResponse;
import com.rikai.backend.dto.response.dashboard.ChartDataResponse;
import com.rikai.backend.dto.response.dashboard.MultiSeriesChartResponse;
import com.rikai.backend.event.*;
import com.rikai.backend.model.Department;
import com.rikai.backend.model.EvaluationSession;
import com.rikai.backend.model.Intern;
import com.rikai.backend.model.InternStatusHistory;
import com.rikai.backend.model.Position;
import com.rikai.backend.model.WeeklyReport;
import com.rikai.backend.model.Enum.SessionType;
import com.rikai.backend.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DashboardService implements IDashboardService {

    InternRepository internRepository;
    UsersRepository usersRepository;
    WeeklyReportRepository weeklyReportRepository;
    EvaluationSessionRepository evaluationSessionRepository;
    InternStatusHistoryRepository internStatusHistoryRepository;
    DepartmentRepository departmentRepository;
    PositionRepository positionRepository;

    /**
     * Fetch recent activities including intern creations, updates, deletions,
     * weekly report creations/updates, evaluation session creations/updates,
     * and intern status changes within the last 7 days.
     * <p>
     * This method optimizes data retrieval by:
     * - Limiting the number of records fetched per entity type.
     * - Using database-level sorting to minimize in-memory operations.
     * - Reducing the number of queries with JOIN FETCH where applicable.
     *
     * @param limit the maximum number of recent activities to return
     * @return a list of recent activity responses sorted by timestamp descending
     */

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentActivities(int limit) {
        List<ActivityResponse> activities = new ArrayList<>();
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        // Apply limit to each sub-query to prevent over-fetching,
        // but fetch a slightly larger buffer (limit) to ensure enough data after merging and re-sorting.
        // Sort at the DB level to retrieve the most recent records efficiently.
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRequest updatePageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        // 1. Interns Created
        List<Intern> recentInterns = internRepository.findByCreatedAtAfter(sevenDaysAgo, pageRequest);
        for (Intern intern : recentInterns) {
            activities.add(ActivityResponse.builder()
                    .type("new")
                    .text(String.format("<strong>%s</strong> was added", intern.getFullName()))
                    .timestamp(intern.getCreatedAt())
                    .internName(intern.getFullName())
                    .internId(intern.getId())
                    .build());
        }

        // 2. Weekly Reports Created (Optimized with JOIN FETCH)
        List<WeeklyReport> recentReports = weeklyReportRepository.findByCreatedAtAfter(sevenDaysAgo, pageRequest);
        for (WeeklyReport report : recentReports) {
            activities.add(ActivityResponse.builder()
                    .type("evaluation")
                    .text(String.format("Mentor <strong>%s</strong> completed weekly evaluation for <strong>%s</strong>",
                            report.getMentor().getFullName(), // No secondary query triggered
                            report.getIntern().getFullName()))
                    .timestamp(report.getCreatedAt())
                    .internName(report.getIntern().getFullName())
                    .mentorName(report.getMentor().getFullName())
                    .internId(report.getIntern().getId())
                    .build());
        }

        // 3. Evaluation Sessions Created (Optimized with JOIN FETCH)
        List<EvaluationSession> recentSessions = evaluationSessionRepository.findByCreatedAtAfter(sevenDaysAgo, pageRequest);
        for (EvaluationSession session : recentSessions) {
            activities.add(ActivityResponse.builder()
                    .type("evaluation")
                    .text(String.format("Mentor <strong>%s</strong> completed %s evaluation for <strong>%s</strong>",
                            session.getMentor().getFullName(),
                            formatSessionType(session.getSessionType()),
                            session.getIntern().getFullName()))
                    .timestamp(session.getCreatedAt())
                    .internName(session.getIntern().getFullName())
                    .mentorName(session.getMentor().getFullName())
                    .internId(session.getIntern().getId())
                    .build());
        }

        // 4. Updated Interns
        // Filter logic (updatedAt != createdAt) in Java as complex SQL checks aren't necessary for small datasets
        List<Intern> updatedInterns = internRepository.findRecentlyUpdated(sevenDaysAgo, updatePageRequest);
        for (Intern intern : updatedInterns) {
            if (!intern.getUpdatedAt().equals(intern.getCreatedAt())) {
                activities.add(ActivityResponse.builder()
                        .type("system")
                        .text(String.format("<strong>%s</strong> was updated", intern.getFullName()))
                        .timestamp(intern.getUpdatedAt())
                        .internName(intern.getFullName())
                        .internId(intern.getId())
                        .build());
            }
        }

        // 5. Updated Reports
        List<WeeklyReport> updatedReports = weeklyReportRepository.findByUpdatedAtAfter(sevenDaysAgo, updatePageRequest);
        for (WeeklyReport report : updatedReports) {
            if (!report.getUpdatedAt().equals(report.getCreatedAt())) {
                activities.add(ActivityResponse.builder()
                        .type("evaluation")
                        .text(String.format("Weekly evaluation for <strong>%s</strong> was updated by <strong>%s</strong>",
                                report.getIntern().getFullName(),
                                report.getMentor().getFullName()))
                        .timestamp(report.getUpdatedAt())
                        .internName(report.getIntern().getFullName())
                        .mentorName(report.getMentor().getFullName())
                        .internId(report.getIntern().getId())
                        .build());
            }
        }

        // 6. Updated Sessions
        List<EvaluationSession> updatedSessions = evaluationSessionRepository.findByUpdatedAtAfter(sevenDaysAgo, updatePageRequest);
        for (EvaluationSession session : updatedSessions) {
            if (!session.getUpdatedAt().equals(session.getCreatedAt())) {
                activities.add(ActivityResponse.builder()
                        .type("evaluation")
                        .text(String.format("%s evaluation for <strong>%s</strong> was updated by <strong>%s</strong>",
                                formatSessionType(session.getSessionType()),
                                session.getIntern().getFullName(),
                                session.getMentor().getFullName()))
                        .timestamp(session.getUpdatedAt())
                        .internName(session.getIntern().getFullName())
                        .mentorName(session.getMentor().getFullName())
                        .internId(session.getIntern().getId())
                        .build());
            }
        }

        // 7. Deleted Interns
        List<Intern> deletedInterns = internRepository.findRecentlyDeleted(sevenDaysAgo, updatePageRequest);
        for (Intern intern : deletedInterns) {
            activities.add(ActivityResponse.builder()
                    .type("warning")
                    .text(String.format("<strong>%s</strong> was removed", intern.getFullName()))
                    .timestamp(intern.getUpdatedAt())
                    .internName(intern.getFullName())
                    .internId(intern.getId())
                    .build());
        }

        // 8. Status Changes (Maintain existing logic but ensure Repository is optimized)
        PageRequest statusPageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "changedAt"));
        // Assumes findRecentStatusChanges uses @Query with JOIN FETCH for the intern entity
        List<InternStatusHistory> recentStatusChanges = internStatusHistoryRepository
                .findRecentStatusChanges(sevenDaysAgo, statusPageRequest)
                .getContent();

        for (InternStatusHistory history : recentStatusChanges) {
            String activityType = switch (history.getNewStatus().toString()) {
                case "WARNING" -> "warning";
                case "COMPLETE" -> "completed";
                default -> "status_change";
            };

            activities.add(ActivityResponse.builder()
                    .type(activityType)
                    .text(String.format("Status of <strong>%s</strong> changed to <strong>%s</strong>",
                            history.getIntern().getFullName(),
                            history.getNewStatus()))
                    .timestamp(history.getChangedAt())
                    .internName(history.getIntern().getFullName())
                    .internId(history.getIntern().getId())
                    .oldStatus(history.getOldStatus() != null ? history.getOldStatus().toString() : null)
                    .newStatus(history.getNewStatus().toString())
                    .build());
        }

        // Final Merge, Sort, and Global Limit
        return activities.stream()
                .sorted(Comparator.comparing(ActivityResponse::getTimestamp).reversed())
                .limit(limit)
                .toList();
    }

    private String formatSessionType(SessionType sessionType) {
        if (sessionType == null) {
            return "evaluation";
        }
        String name = sessionType.toString();
        // Convert FIRST_TERM -> First Term, MID_TERM -> Mid Term, FINAL -> Final
        String[] parts = name.split("_");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            String part = parts[i];
            if (part.length() > 0) {
                formatted.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    formatted.append(part.substring(1).toLowerCase());
                }
            }
        }
        return formatted.toString();
    }


    @EventListener
    @Async
    @Transactional
    public void handleEvaluationSessionCreated(EvaluationSessionCreatedEvent event) {

    }

    @EventListener
    @Async
    @Transactional
    public void handleEvaluationSessionUpdated(EvaluationSessionUpdatedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleEvaluationSessionDeleted(EvaluationSessionDeletedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleWeeklyReportCreated(WeeklyReportCreatedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleWeeklyReportUpdated(WeeklyReportUpdatedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleWeeklyReportDeleted(WeeklyReportDeletedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleInternCreated(InternCreatedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleInternUpdated(InternUpdatedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleInternDeleted(InternDeletedEvent event) {
    }

    @EventListener
    @Async
    @Transactional
    public void handleInternStatusChanged(InternStatusChangedEvent event) {
    }

    /**
     * Get the number of mentors grouped by their respective departments.
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getMentorsByDepartment() {
        List<Department> departments = departmentRepository.findAll();
        List<ChartDataResponse.ChartItem> items = new ArrayList<>();
        for (Department dept : departments) {
            // Count interns by department (through position)
            long count = usersRepository.countByDepartment_Id(dept.getId());
            if (count > 0) {
                items.add(ChartDataResponse.ChartItem.builder()
                        .label(dept.getTitle())
                        .value(count)
                        .build());
            }
        }
        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the number of interns grouped by their respective positions.
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getInternsByPosition() {
        List<Position> positions = positionRepository.findAll();
        List<ChartDataResponse.ChartItem> items = new ArrayList<>();
        for (Position position : positions) {
            long count = internRepository.countByPosition_Id(position.getId());
            if (count > 0) {
                items.add(ChartDataResponse.ChartItem.builder()
                        .label(position.getTitle())
                        .value(count)
                        .build());
            }
        }
        // Sort by value descending
        items.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the trend of intern additions over the past N months.
     *
     * @param months Number of months to look back
     * @return ChartDataResponse containing monthly intern addition counts
     */

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getInternsTrend(int months) {
        // Calculate start date (N months ago)
        YearMonth startMonth = YearMonth.now().minusMonths(months - 1);
        YearMonth endMonth = YearMonth.now();

        // Get all interns created in the date range
        Instant startInstant = startMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Intern> interns = internRepository.findAll().stream()
                .filter(intern -> intern.getCreatedAt() != null
                        && !intern.getCreatedAt().isBefore(startInstant)
                        && !intern.getCreatedAt().isAfter(endInstant))
                .toList();

        // Group by year-month
        Map<YearMonth, Long> monthlyCounts = interns.stream()
                .collect(Collectors.groupingBy(
                        intern -> YearMonth.from(intern.getCreatedAt().atZone(ZoneId.systemDefault())),
                        Collectors.counting()
                ));

        // Fill in missing months with 0
        Map<YearMonth, Long> filledCounts = new LinkedHashMap<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            filledCounts.put(current, monthlyCounts.getOrDefault(current, 0L));
            current = current.plusMonths(1);
        }

        // Convert to ChartItem list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<ChartDataResponse.ChartItem> items = filledCounts.entrySet().stream()
                .map(entry -> ChartDataResponse.ChartItem.builder()
                        .label(entry.getKey().format(formatter))
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the trend of intern additions over a custom date range specified by YearMonth.
     *
     * @param startMonth Start month of the range
     * @param endMonth   End month of the range
     * @return ChartDataResponse containing monthly intern addition counts
     */

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getInternsTrendByDateRange(YearMonth startMonth, YearMonth endMonth) {
        // Get all interns created in the date range
        Instant startInstant = startMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Intern> interns = internRepository.findAll().stream()
                .filter(intern -> intern.getCreatedAt() != null
                        && !intern.getCreatedAt().isBefore(startInstant)
                        && !intern.getCreatedAt().isAfter(endInstant))
                .toList();

        // Group by year-month
        Map<YearMonth, Long> monthlyCounts = interns.stream()
                .collect(Collectors.groupingBy(
                        intern -> YearMonth.from(intern.getCreatedAt().atZone(ZoneId.systemDefault())),
                        Collectors.counting()
                ));

        // Fill in missing months with 0
        Map<YearMonth, Long> filledCounts = new LinkedHashMap<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            filledCounts.put(current, monthlyCounts.getOrDefault(current, 0L));
            current = current.plusMonths(1);
        }

        // Convert to ChartItem list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<ChartDataResponse.ChartItem> items = filledCounts.entrySet().stream()
                .map(entry -> ChartDataResponse.ChartItem.builder()
                        .label(entry.getKey().format(formatter))
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the trend of intern additions over a custom date range specified by LocalDate.
     *
     * @param startDate Start date of the range
     * @param endDate   End date of the range
     * @return ChartDataResponse containing daily intern addition counts
     */

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getInternsTrendByDayRange(LocalDate startDate, LocalDate endDate) {
        // Get all interns created in the date range
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Intern> interns = internRepository.findAll().stream()
                .filter(intern -> intern.getCreatedAt() != null
                        && !intern.getCreatedAt().isBefore(startInstant)
                        && !intern.getCreatedAt().isAfter(endInstant))
                .toList();

        // Group by day
        Map<LocalDate, Long> dailyCounts = interns.stream()
                .collect(Collectors.groupingBy(
                        intern -> intern.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));

        // Fill in missing days with 0
        Map<LocalDate, Long> filledCounts = new LinkedHashMap<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            filledCounts.put(current, dailyCounts.getOrDefault(current, 0L));
            current = current.plusDays(1);
        }

        // Convert to ChartItem list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        List<ChartDataResponse.ChartItem> items = filledCounts.entrySet().stream()
                .map(entry -> ChartDataResponse.ChartItem.builder()
                        .label(entry.getKey().format(formatter))
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the trend of average weekly scores over the past N months.
     *
     * @param months Number of months to look back
     * @return ChartDataResponse containing monthly average scores
     */

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getAverageScoreTrend(int months) {
        // Calculate start date (N months ago)
        YearMonth startMonth = YearMonth.now().minusMonths(months - 1);
        YearMonth endMonth = YearMonth.now();

        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDate = endMonth.atEndOfMonth();

        // Get all weekly reports in the date range
        List<WeeklyReport> reports = weeklyReportRepository.findAll().stream()
                .filter(report -> report.getWeekStartDate() != null
                        && !report.getWeekStartDate().isBefore(startDate)
                        && !report.getWeekStartDate().isAfter(endDate)
                        && report.getAverageScore() != null
                        && report.getAverageScore().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        // Group by month and calculate average score
        Map<YearMonth, List<BigDecimal>> monthlyScores = reports.stream()
                .collect(Collectors.groupingBy(
                        report -> YearMonth.from(report.getWeekStartDate()),
                        Collectors.mapping(WeeklyReport::getAverageScore, Collectors.toList())
                ));

        // Fill in missing months and calculate averages
        Map<YearMonth, BigDecimal> filledAverages = new LinkedHashMap<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            List<BigDecimal> scores = monthlyScores.getOrDefault(current, Collections.emptyList());
            BigDecimal average = scores.isEmpty()
                    ? BigDecimal.ZERO
                    : scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            filledAverages.put(current, average);
            current = current.plusMonths(1);
        }

        // Convert to ChartItem list
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<ChartDataResponse.ChartItem> items = filledAverages.entrySet().stream()
                .map(entry -> ChartDataResponse.ChartItem.builder()
                        .label(entry.getKey().format(formatter))
                        .value(entry.getValue().multiply(BigDecimal.valueOf(100)).longValue()) // Store as integer (score * 100)
                        .build())
                .collect(Collectors.toList());

        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the trend of intern completion rates over the past N months.
     *
     * @param months Number of months to look back
     * @return ChartDataResponse containing monthly completion rates
     */

    @Override
    @Transactional(readOnly = true)
    public ChartDataResponse getCompletionRateTrend(int months) {
        // Calculate start date (N months ago)
        YearMonth startMonth = YearMonth.now().minusMonths(months - 1);
        YearMonth endMonth = YearMonth.now();

        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDate = endMonth.atEndOfMonth();

        // Get all interns
        List<Intern> allInterns = internRepository.findAll();

        // Group by month and calculate completion rate
        Map<YearMonth, Long> monthlyCompleted = new LinkedHashMap<>();
        Map<YearMonth, Long> monthlyTotal = new LinkedHashMap<>();

        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();

            // Count interns that existed at the end of this month
            long totalInMonth = allInterns.stream()
                    .filter(intern -> intern.getCreatedAt() != null
                            && intern.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(monthEnd.plusDays(1)))
                    .count();

            // Count interns completed by end of this month
            long completedInMonth = allInterns.stream()
                    .filter(intern -> intern.getInternStatus() == InternStatus.COMPLETED
                            && intern.getUpdatedAt() != null
                            && intern.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(monthEnd.plusDays(1))
                            && intern.getCreatedAt() != null
                            && intern.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(monthEnd.plusDays(1)))
                    .count();

            monthlyTotal.put(current, totalInMonth);
            monthlyCompleted.put(current, completedInMonth);
            current = current.plusMonths(1);
        }

        // Calculate completion rate percentages
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        List<ChartDataResponse.ChartItem> items = new ArrayList<>();

        for (Map.Entry<YearMonth, Long> entry : monthlyTotal.entrySet()) {
            YearMonth month = entry.getKey();
            long total = entry.getValue();
            long completed = monthlyCompleted.getOrDefault(month, 0L);

            long rate = total > 0 ? Math.round((completed * 100.0) / total) : 0;

            items.add(ChartDataResponse.ChartItem.builder()
                    .label(month.format(formatter))
                    .value(rate)
                    .build());
        }

        return ChartDataResponse.builder()
                .items(items)
                .build();
    }

    /**
     * Get the trend of average weekly scores over the past N months, grouped by intern position.
     *
     * @param months Number of months to look back
     * @return MultiSeriesChartResponse containing monthly average scores per position
     */
    @Override
    @Transactional(readOnly = true)
    public MultiSeriesChartResponse getAverageScoreTrendByGroup(int months) {
        YearMonth startMonth = YearMonth.now().minusMonths(months - 1);
        YearMonth endMonth = YearMonth.now();
        LocalDate startDate = startMonth.atDay(1);
        LocalDate endDate = endMonth.atEndOfMonth();

        // Get all weekly reports in the date range with valid scores
        List<WeeklyReport> reports = weeklyReportRepository.findAll().stream()
                .filter(report -> report.getWeekStartDate() != null
                        && !report.getWeekStartDate().isBefore(startDate)
                        && !report.getWeekStartDate().isAfter(endDate)
                        && report.getAverageScore() != null
                        && report.getAverageScore().compareTo(BigDecimal.ZERO) > 0
                        && report.getIntern() != null)
                .toList();

        // Generate month labels
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            labels.add(current.format(formatter));
            current = current.plusMonths(1);
        }

        // Group by position
        List<MultiSeriesChartResponse.Series> seriesList = new ArrayList<>();
        List<Position> positions = positionRepository.findAll();

        for (Position pos : positions) {
            List<WeeklyReport> posReports = reports.stream()
                    .filter(r -> r.getIntern().getPosition() != null
                            && r.getIntern().getPosition().getId().equals(pos.getId()))
                    .toList();

            if (posReports.isEmpty()) continue;

            List<Double> data = calculateMonthlyAverages(posReports, startMonth, endMonth);

            seriesList.add(MultiSeriesChartResponse.Series.builder()
                    .name(pos.getTitle())
                    .data(data)
                    .build());
        }

        return MultiSeriesChartResponse.builder()
                .labels(labels)
                .series(seriesList)
                .build();
    }

    /**
     * Calculate monthly average scores from weekly reports within the specified date range.
     *
     * @param reports    List of weekly reports
     * @param startMonth Start month of the range
     * @param endMonth   End month of the range
     * @return List of average scores per month
     */
    private List<Double> calculateMonthlyAverages(List<WeeklyReport> reports, YearMonth startMonth, YearMonth endMonth) {
        Map<YearMonth, List<BigDecimal>> monthlyScores = reports.stream()
                .collect(Collectors.groupingBy(
                        report -> YearMonth.from(report.getWeekStartDate()),
                        Collectors.mapping(WeeklyReport::getAverageScore, Collectors.toList())
                ));

        List<Double> data = new ArrayList<>();
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            List<BigDecimal> scores = monthlyScores.getOrDefault(current, Collections.emptyList());
            double average = scores.isEmpty() ? 0.0
                    : scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP)
                    .doubleValue();
            data.add(average);
            current = current.plusMonths(1);
        }
        return data;
    }

    /**
     * Get the trend of intern completion rates over the past N months, grouped by intern position.
     *
     * @param months Number of months to look back
     * @return MultiSeriesChartResponse containing monthly completion rates per position
     */
    @Override
    @Transactional(readOnly = true)
    public MultiSeriesChartResponse getCompletionRateTrendByGroup(int months) {
        YearMonth startMonth = YearMonth.now().minusMonths(months - 1);
        YearMonth endMonth = YearMonth.now();

        // Get all interns
        List<Intern> allInterns = internRepository.findAll();

        // Generate month labels
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            labels.add(current.format(formatter));
            current = current.plusMonths(1);
        }

        // Group by position
        List<MultiSeriesChartResponse.Series> seriesList = new ArrayList<>();
        List<Position> positions = positionRepository.findAll();

        for (Position pos : positions) {
            List<Intern> posInterns = allInterns.stream()
                    .filter(i -> i.getPosition() != null
                            && i.getPosition().getId().equals(pos.getId()))
                    .toList();

            if (posInterns.isEmpty()) continue;

            List<Double> data = calculateMonthlyCompletionRates(posInterns, startMonth, endMonth);

            seriesList.add(MultiSeriesChartResponse.Series.builder()
                    .name(pos.getTitle())
                    .data(data)
                    .build());
        }

        return MultiSeriesChartResponse.builder()
                .labels(labels)
                .series(seriesList)
                .build();
    }

    /**
     * Calculate monthly completion rates from interns within the specified date range.
     *
     * @param interns    List of interns
     * @param startMonth Start month of the range
     * @param endMonth   End month of the range
     * @return List of completion rates per month
     */
    private List<Double> calculateMonthlyCompletionRates(List<Intern> interns, YearMonth startMonth, YearMonth endMonth) {
        List<Double> data = new ArrayList<>();
        YearMonth current = startMonth;

        while (!current.isAfter(endMonth)) {
            LocalDate monthEnd = current.atEndOfMonth();

            // Count interns that existed at the end of this month
            long totalInMonth = interns.stream()
                    .filter(intern -> intern.getCreatedAt() != null
                            && intern.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(monthEnd.plusDays(1)))
                    .count();

            // Count interns completed by end of this month
            long completedInMonth = interns.stream()
                    .filter(intern -> intern.getInternStatus() == InternStatus.COMPLETED
                            && intern.getUpdatedAt() != null
                            && intern.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(monthEnd.plusDays(1))
                            && intern.getCreatedAt() != null
                            && intern.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(monthEnd.plusDays(1)))
                    .count();

            double rate = totalInMonth > 0 ? Math.round((completedInMonth * 100.0) / totalInMonth) : 0;
            data.add(rate);

            current = current.plusMonths(1);
        }
        return data;
    }
}
