package com.rikai.backend.service.dashboard;

import com.rikai.backend.common.ErrorCode;
import com.rikai.backend.common.InternStatus;
import com.rikai.backend.dto.response.dashboard.ActivityResponse;
import com.rikai.backend.dto.response.dashboard.ChartDataResponse;
import com.rikai.backend.dto.response.dashboard.MentorStatisticsResponse;
import com.rikai.backend.dto.response.dashboard.MultiSeriesChartResponse;
import com.rikai.backend.event.*;
import com.rikai.backend.exception.AppException;
import com.rikai.backend.model.*;
import com.rikai.backend.model.Enum.BatchStatus;
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
import java.util.UUID;
import java.util.stream.Collectors;
import com.rikai.backend.service.auth.AuthenticationService;

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
        InternshipBatchRepository internshipBatchRepository;
        AuthenticationService authenticationService;
        AuditLogRepository auditLogRepository;

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
                PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
                return auditLogRepository.findAll(pageRequest).getContent().stream()
                                .map(this::mapToActivityResponse)
                                .toList();
        }

        private ActivityResponse mapToActivityResponse(AuditLog log) {
                String type = "system";
                String action = log.getAction();
                String entityType = log.getEntityType();
                String details = log.getDetails();

                if ("CREATE".equals(action) && "INTERN".equals(entityType)) {
                        type = "new";
                } else if ("DELETE".equals(action) && "INTERN".equals(entityType)) {
                        type = "warning";
                } else if ("WEEKLY_REPORT".equals(entityType) || "EVALUATION_SESSION".equals(entityType)) {
                        type = "evaluation";
                } else if ("STATUS_CHANGE".equals(action)) {
                        if (details != null) {
                                if (details.contains("WARNING")) {
                                        type = "warning";
                                } else if (details.contains("COMPLETE")) {
                                        type = "completed";
                                }
                        }
                        type = "status_change";
                } else if ("PUBLISH".equals(action)) {
                        type = "completed";
                }

                return ActivityResponse.builder()
                                .type(type)
                                .text(details)
                                .timestamp(log.getCreatedAt())
                                .build();
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
        public void handleCRUDInternEvent(InternCudEvent event) {
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
                Instant endInstant = endMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault())
                                .toInstant();

                List<Intern> interns = internRepository.findAll().stream()
                                .filter(intern -> intern.getCreatedAt() != null
                                                && !intern.getCreatedAt().isBefore(startInstant)
                                                && !intern.getCreatedAt().isAfter(endInstant))
                                .toList();

                // Group by year-month
                Map<YearMonth, Long> monthlyCounts = interns.stream()
                                .collect(Collectors.groupingBy(
                                                intern -> YearMonth.from(
                                                                intern.getCreatedAt().atZone(ZoneId.systemDefault())),
                                                Collectors.counting()));

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
         * Get the trend of intern additions over a custom date range specified by
         * YearMonth.
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
                Instant endInstant = endMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault())
                                .toInstant();

                List<Intern> interns = internRepository.findAll().stream()
                                .filter(intern -> intern.getCreatedAt() != null
                                                && !intern.getCreatedAt().isBefore(startInstant)
                                                && !intern.getCreatedAt().isAfter(endInstant))
                                .toList();

                // Group by year-month
                Map<YearMonth, Long> monthlyCounts = interns.stream()
                                .collect(Collectors.groupingBy(
                                                intern -> YearMonth.from(
                                                                intern.getCreatedAt().atZone(ZoneId.systemDefault())),
                                                Collectors.counting()));

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
         * Get the trend of intern additions over a custom date range specified by
         * LocalDate.
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
                                                intern -> intern.getCreatedAt().atZone(ZoneId.systemDefault())
                                                                .toLocalDate(),
                                                Collectors.counting()));

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
                                                Collectors.mapping(WeeklyReport::getAverageScore,
                                                                Collectors.toList())));

                // Fill in missing months and calculate averages
                Map<YearMonth, BigDecimal> filledAverages = new LinkedHashMap<>();
                YearMonth current = startMonth;
                while (!current.isAfter(endMonth)) {
                        List<BigDecimal> scores = monthlyScores.getOrDefault(current, Collections.emptyList());
                        BigDecimal average = scores.isEmpty()
                                        ? BigDecimal.ZERO
                                        : scores.stream()
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                        .divide(BigDecimal.valueOf(scores.size()), 2,
                                                                        RoundingMode.HALF_UP);
                        filledAverages.put(current, average);
                        current = current.plusMonths(1);
                }

                // Convert to ChartItem list
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                List<ChartDataResponse.ChartItem> items = filledAverages.entrySet().stream()
                                .map(entry -> ChartDataResponse.ChartItem.builder()
                                                .label(entry.getKey().format(formatter))
                                                .value(entry.getValue().multiply(BigDecimal.valueOf(100)).longValue()) // Store
                                                                                                                       // as
                                                                                                                       // integer
                                                                                                                       // (score
                                                                                                                       // *
                                                                                                                       // 100)
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
                                                        && intern.getCreatedAt().atZone(ZoneId.systemDefault())
                                                                        .toLocalDate().isBefore(monthEnd.plusDays(1)))
                                        .count();

                        // Count interns completed by end of this month
                        long completedInMonth = allInterns.stream()
                                        .filter(intern -> intern.getInternStatus() == InternStatus.COMPLETED
                                                        && intern.getUpdatedAt() != null
                                                        && intern.getUpdatedAt().atZone(ZoneId.systemDefault())
                                                                        .toLocalDate().isBefore(monthEnd.plusDays(1))
                                                        && intern.getCreatedAt() != null
                                                        && intern.getCreatedAt().atZone(ZoneId.systemDefault())
                                                                        .toLocalDate().isBefore(monthEnd.plusDays(1)))
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
         * Get the trend of average weekly scores over the past N months, grouped by
         * intern position.
         *
         * @param months Number of months to look back
         * @return MultiSeriesChartResponse containing monthly average scores per
         *         position
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

                        if (posReports.isEmpty())
                                continue;

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
         * Calculate monthly average scores from weekly reports within the specified
         * date range.
         *
         * @param reports    List of weekly reports
         * @param startMonth Start month of the range
         * @param endMonth   End month of the range
         * @return List of average scores per month
         */
        private List<Double> calculateMonthlyAverages(List<WeeklyReport> reports, YearMonth startMonth,
                        YearMonth endMonth) {
                Map<YearMonth, List<BigDecimal>> monthlyScores = reports.stream()
                                .collect(Collectors.groupingBy(
                                                report -> YearMonth.from(report.getWeekStartDate()),
                                                Collectors.mapping(WeeklyReport::getAverageScore,
                                                                Collectors.toList())));

                List<Double> data = new ArrayList<>();
                YearMonth current = startMonth;
                while (!current.isAfter(endMonth)) {
                        List<BigDecimal> scores = monthlyScores.getOrDefault(current, Collections.emptyList());
                        double average = scores.isEmpty() ? 0.0
                                        : scores.stream()
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                        .divide(BigDecimal.valueOf(scores.size()), 4,
                                                                        RoundingMode.HALF_UP)
                                                        .doubleValue();
                        data.add(average);
                        current = current.plusMonths(1);
                }
                return data;
        }

        /**
         * Get the trend of intern completion rates over the past N months, grouped by
         * intern position.
         *
         * @param months Number of months to look back
         * @return MultiSeriesChartResponse containing monthly completion rates per
         *         position
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

                        if (posInterns.isEmpty())
                                continue;

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
         * Calculate monthly completion rates from interns within the specified date
         * range.
         *
         * @param interns    List of interns
         * @param startMonth Start month of the range
         * @param endMonth   End month of the range
         * @return List of completion rates per month
         */
        private List<Double> calculateMonthlyCompletionRates(List<Intern> interns, YearMonth startMonth,
                        YearMonth endMonth) {
                List<Double> data = new ArrayList<>();
                YearMonth current = startMonth;

                while (!current.isAfter(endMonth)) {
                        LocalDate monthEnd = current.atEndOfMonth();

                        // Count interns that existed at the end of this month
                        long totalInMonth = interns.stream()
                                        .filter(intern -> intern.getCreatedAt() != null
                                                        && intern.getCreatedAt().atZone(ZoneId.systemDefault())
                                                                        .toLocalDate().isBefore(monthEnd.plusDays(1)))
                                        .count();

                        // Count interns completed by end of this month
                        long completedInMonth = interns.stream()
                                        .filter(intern -> intern.getInternStatus() == InternStatus.COMPLETED
                                                        && intern.getUpdatedAt() != null
                                                        && intern.getUpdatedAt().atZone(ZoneId.systemDefault())
                                                                        .toLocalDate().isBefore(monthEnd.plusDays(1))
                                                        && intern.getCreatedAt() != null
                                                        && intern.getCreatedAt().atZone(ZoneId.systemDefault())
                                                                        .toLocalDate().isBefore(monthEnd.plusDays(1)))
                                        .count();

                        double rate = totalInMonth > 0 ? Math.round((completedInMonth * 100.0) / totalInMonth) : 0;
                        data.add(rate);

                        current = current.plusMonths(1);
                }
                return data;
        }

        @Override
        @Transactional(readOnly = true)
        public MultiSeriesChartResponse getBatchScoreTrend() {
                // Get all ONGOING batches with their interns
                List<InternshipBatch> ongoingBatches = internshipBatchRepository
                                .findByStatusWithInterns(BatchStatus.ONGOING);

                if (ongoingBatches.isEmpty()) {
                        return MultiSeriesChartResponse.builder()
                                        .labels(Collections.emptyList())
                                        .series(Collections.emptyList())
                                        .build();
                }

                // Find the earliest start date and calculate weeks until now
                LocalDate now = LocalDate.now();
                LocalDate earliestStart = ongoingBatches.stream()
                                .map(InternshipBatch::getStartDate)
                                .min(LocalDate::compareTo)
                                .orElse(now);

                // Calculate number of weeks from earliest start to now
                long totalWeeks = ChronoUnit.WEEKS.between(earliestStart, now) + 1;
                if (totalWeeks <= 0)
                        totalWeeks = 1;

                // Generate week labels with date format DD/MM/YYYY
                List<String> labels = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (int i = 0; i < totalWeeks; i++) {
                        LocalDate weekStart = earliestStart.plusWeeks(i);
                        labels.add(weekStart.format(formatter));
                }

                // Build series for each batch
                List<MultiSeriesChartResponse.Series> seriesList = new ArrayList<>();

                for (InternshipBatch batch : ongoingBatches) {
                        LocalDate batchStart = batch.getStartDate();
                        List<Long> internIds = batch.getInterns() != null
                                        ? batch.getInterns().stream().map(Intern::getId).toList()
                                        : Collections.emptyList();

                        if (internIds.isEmpty())
                                continue;

                        // Get all weekly reports for interns in this batch
                        List<WeeklyReport> batchReports = weeklyReportRepository.findAll().stream()
                                        .filter(r -> r.getIntern() != null && internIds.contains(r.getIntern().getId()))
                                        .filter(r -> r.getWeekStartDate() != null && r.getAverageScore() != null)
                                        .filter(r -> !r.getWeekStartDate().isBefore(batchStart)
                                                        && !r.getWeekStartDate().isAfter(now))
                                        .toList();

                        // Calculate average score for each week
                        // Labels are generated from earliestStart, so we need to match each label with
                        // batch's data
                        List<Double> data = new ArrayList<>();
                        for (int i = 0; i < totalWeeks; i++) {
                                // Calculate the week start date for this label (based on earliestStart)
                                LocalDate labelWeekStart = earliestStart.plusWeeks(i);
                                LocalDate labelWeekEnd = labelWeekStart.plusDays(6);

                                // If this week is before batch start date, add null
                                if (labelWeekStart.isBefore(batchStart)) {
                                        data.add(null);
                                        continue;
                                }

                                // If this week is in the future, add null
                                if (labelWeekStart.isAfter(now)) {
                                        data.add(null);
                                        continue;
                                }

                                // Find reports for this week
                                final LocalDate finalWeekStart = labelWeekStart;
                                final LocalDate finalWeekEnd = labelWeekEnd;
                                List<BigDecimal> weekScores = batchReports.stream()
                                                .filter(r -> !r.getWeekStartDate().isBefore(finalWeekStart)
                                                                && !r.getWeekStartDate().isAfter(finalWeekEnd))
                                                .map(WeeklyReport::getAverageScore)
                                                .filter(score -> score != null && score.compareTo(BigDecimal.ZERO) > 0)
                                                .toList();

                                if (weekScores.isEmpty()) {
                                        data.add(null);
                                } else {
                                        double avg = weekScores.stream()
                                                        .mapToDouble(BigDecimal::doubleValue)
                                                        .average()
                                                        .orElse(0.0);
                                        data.add(Math.round(avg * 100.0) / 100.0);
                                }
                        }

                        seriesList.add(MultiSeriesChartResponse.Series.builder()
                                        .id(batch.getId())
                                        .name(batch.getName())
                                        .data(data)
                                        .build());
                }

                return MultiSeriesChartResponse.builder()
                                .labels(labels)
                                .series(seriesList)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public MultiSeriesChartResponse getInternScoreTrendByBatch(Long batchId) {
                // Get the batch with interns
                InternshipBatch batch = internshipBatchRepository.findById(batchId)
                                .orElse(null);

                if (batch == null || batch.getInterns() == null || batch.getInterns().isEmpty()) {
                        return MultiSeriesChartResponse.builder()
                                        .labels(Collections.emptyList())
                                        .series(Collections.emptyList())
                                        .build();
                }

                LocalDate batchStart = batch.getStartDate();
                LocalDate now = LocalDate.now();

                // Calculate number of weeks from batch start to now
                long totalWeeks = ChronoUnit.WEEKS.between(batchStart, now) + 1;
                if (totalWeeks <= 0)
                        totalWeeks = 1;

                // Generate week labels with date format DD/MM/YYYY
                List<String> labels = new ArrayList<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (int i = 0; i < totalWeeks; i++) {
                        LocalDate weekStart = batchStart.plusWeeks(i);
                        labels.add(weekStart.format(formatter));
                }

                // Build series for each intern
                List<MultiSeriesChartResponse.Series> seriesList = new ArrayList<>();

                for (Intern intern : batch.getInterns()) {
                        LocalDate internStartDate = intern.getStartDate();

                        // Get all weekly reports for this intern
                        List<WeeklyReport> internReports = weeklyReportRepository.findAll().stream()
                                        .filter(r -> r.getIntern() != null
                                                        && r.getIntern().getId().equals(intern.getId()))
                                        .filter(r -> r.getWeekStartDate() != null && r.getAverageScore() != null)
                                        .filter(r -> !r.getWeekStartDate().isBefore(internStartDate)
                                                        && !r.getWeekStartDate().isAfter(now))
                                        .toList();

                        // Calculate score for each week
                        List<Double> data = new ArrayList<>();
                        for (int weekNum = 1; weekNum <= totalWeeks; weekNum++) {
                                LocalDate weekStart = batchStart.plusWeeks(weekNum - 1);
                                LocalDate weekEnd = weekStart.plusDays(6);

                                // If intern hasn't started yet or week is in the future, add null
                                if (weekStart.isBefore(internStartDate) || weekStart.isAfter(now)) {
                                        data.add(null);
                                        continue;
                                }

                                // Find report for this week
                                final LocalDate finalWeekStart = weekStart;
                                final LocalDate finalWeekEnd = weekEnd;
                                WeeklyReport weekReport = internReports.stream()
                                                .filter(r -> !r.getWeekStartDate().isBefore(finalWeekStart)
                                                                && !r.getWeekStartDate().isAfter(finalWeekEnd))
                                                .findFirst()
                                                .orElse(null);

                                if (weekReport == null || weekReport.getAverageScore() == null
                                                || weekReport.getAverageScore().compareTo(BigDecimal.ZERO) <= 0) {
                                        data.add(null);
                                } else {
                                        data.add(weekReport.getAverageScore().doubleValue());
                                }
                        }

                        seriesList.add(MultiSeriesChartResponse.Series.builder()
                                        .id(intern.getId())
                                        .name(intern.getFullName())
                                        .data(data)
                                        .build());
                }

                return MultiSeriesChartResponse.builder()
                                .labels(labels)
                                .series(seriesList)
                                .build();
        }

        // ===== MENTOR DASHBOARD METHODS =====

        @Override
        @Transactional(readOnly = true)
        public MentorStatisticsResponse getMentorStatistics() {
                Users currentUser = authenticationService.getCurrentUser();
                if (currentUser == null) {
                        throw new AppException(ErrorCode.UNAUTHENTICATED);
                }
                UUID mentorId = currentUser.getId();

                long totalInterns = internRepository.countByMentor_Id(mentorId);
                long activeInterns = internRepository.countByMentor_IdAndInternStatus(mentorId, InternStatus.ACTIVE);
                long warningInterns = internRepository.countByMentor_IdAndInternStatus(mentorId, InternStatus.WARNING);

                // Count interns not evaluated this week (pending reports)
                LocalDate today = LocalDate.now();
                LocalDate weekStartDate = today
                                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                long pendingReports = internRepository.findInternsNotEvaluatedThisWeekByMentor(mentorId, weekStartDate,
                                PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

                return MentorStatisticsResponse.builder()
                                .totalInterns(totalInterns)
                                .activeInterns(activeInterns)
                                .warningInterns(warningInterns)
                                .pendingReports(pendingReports)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public ChartDataResponse getMentorAverageScoreTrend(int months) {
                Users currentUser = authenticationService.getCurrentUser();
                if (currentUser == null) {
                        throw new AppException(ErrorCode.UNAUTHENTICATED);
                }
                UUID mentorId = currentUser.getId();

                YearMonth startMonth = YearMonth.now().minusMonths(months - 1);
                YearMonth endMonth = YearMonth.now();

                LocalDate startDate = startMonth.atDay(1);
                LocalDate endDate = endMonth.atEndOfMonth();

                // Get weekly reports for this mentor's interns
                List<WeeklyReport> reports = weeklyReportRepository.findAll().stream()
                                .filter(report -> report.getMentor() != null
                                                && report.getMentor().getId().equals(mentorId))
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
                                                Collectors.mapping(WeeklyReport::getAverageScore,
                                                                Collectors.toList())));

                // Fill in missing months and calculate averages
                Map<YearMonth, BigDecimal> filledAverages = new LinkedHashMap<>();
                YearMonth current = startMonth;
                while (!current.isAfter(endMonth)) {
                        List<BigDecimal> scores = monthlyScores.getOrDefault(current, Collections.emptyList());
                        BigDecimal average = scores.isEmpty()
                                        ? BigDecimal.ZERO
                                        : scores.stream()
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                        .divide(BigDecimal.valueOf(scores.size()), 2,
                                                                        RoundingMode.HALF_UP);
                        filledAverages.put(current, average);
                        current = current.plusMonths(1);
                }

                // Convert to ChartItem list
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
                List<ChartDataResponse.ChartItem> items = filledAverages.entrySet().stream()
                                .map(entry -> ChartDataResponse.ChartItem.builder()
                                                .label(entry.getKey().format(formatter))
                                                .value(entry.getValue().multiply(BigDecimal.valueOf(100)).longValue())
                                                .build())
                                .collect(Collectors.toList());

                return ChartDataResponse.builder()
                                .items(items)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public ChartDataResponse getMentorInternStatusDistribution() {
                Users currentUser = authenticationService.getCurrentUser();
                if (currentUser == null) {
                        throw new AppException(ErrorCode.UNAUTHENTICATED);
                }
                UUID mentorId = currentUser.getId();

                List<ChartDataResponse.ChartItem> items = new ArrayList<>();

                for (InternStatus status : InternStatus.values()) {
                        long count = internRepository.countByMentor_IdAndInternStatus(mentorId, status);
                        if (count > 0) {
                                items.add(ChartDataResponse.ChartItem.builder()
                                                .label(status.name())
                                                .value(count)
                                                .build());
                        }
                }

                return ChartDataResponse.builder()
                                .items(items)
                                .build();
        }
}
