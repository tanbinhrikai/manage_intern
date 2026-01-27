package com.rikai.backend.ai.tools;

import com.rikai.backend.model.Intern;
import com.rikai.backend.model.WeeklyReport;
import com.rikai.backend.model.WeeklyReportDetail;
import com.rikai.backend.repository.InternRepository;
import com.rikai.backend.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Tool for retrieving weekly report analysis.
 * This tool allows the AI agent to get weekly performance data for interns.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportTool {

        private final WeeklyReportRepository weeklyReportRepository;
        private final InternRepository internRepository;

        public record WeeklyReportResult(
                        String internName,
                        List<WeeklyReportInfo> reports,
                        Double overallAverageScore) {
        }

        public record WeeklyReportInfo(
                        Integer weekNumber,
                        String weekStartDate,
                        String tasksAssigned,
                        String tasksCompleted,
                        String issuesRisks,
                        String mentorComment,
                        BigDecimal averageScore,
                        List<CriteriaScore> criteriaScores) {
        }

        public record CriteriaScore(
                        String criteriaName,
                        BigDecimal score,
                        String comment) {
        }

        @Tool(description = """
                        Get weekly report analysis for an intern. Returns weekly performance data including:
                        - Tasks assigned and completed
                        - Issues and risks noted
                        - Mentor's overall comment
                        - Average score for the week
                        - Individual criteria scores

                        Use this tool when user asks:
                        - "What did intern [id] do this week?"
                        - "What is intern [id]'s weekly score?"
                        - "How is intern [id] performing?"
                        - "Show me the weekly report for intern [id]"
                        - "What feedback did the mentor give to intern [id]?"
                        """)
        public WeeklyReportResult getWeeklyReportAnalysis(
                        @ToolParam(description = "ID of the intern to get weekly reports for") Long internId) {

                log.info("AI Tool: getWeeklyReportAnalysis called for internId: {}", internId);

                if (internId == null) {
                        return new WeeklyReportResult("Unknown", Collections.emptyList(), 0.0);
                }

                // Get intern name
                String internName = internRepository.findById(internId)
                                .map(Intern::getFullName)
                                .orElse("Unknown");

                // Get weekly reports using the existing repository method with Pageable
                List<WeeklyReport> reports = weeklyReportRepository.findByInternIdOrderByWeekStartDateDesc(
                                internId, PageRequest.of(0, 20)).getContent();

                if (reports.isEmpty()) {
                        return new WeeklyReportResult(internName, Collections.emptyList(), 0.0);
                }

                List<WeeklyReportInfo> reportInfos = reports.stream()
                                .map(this::mapToReportInfo)
                                .collect(Collectors.toList());

                // Calculate overall average
                double overallAverage = reports.stream()
                                .filter(r -> r.getAverageScore() != null)
                                .mapToDouble(r -> r.getAverageScore().doubleValue())
                                .average()
                                .orElse(0.0);

                return new WeeklyReportResult(internName, reportInfos, overallAverage);
        }

        private WeeklyReportInfo mapToReportInfo(WeeklyReport report) {
                List<CriteriaScore> criteriaScores = report.getDetails() != null
                                ? report.getDetails().stream()
                                                .map(this::mapToCriteriaScore)
                                                .collect(Collectors.toList())
                                : Collections.emptyList();

                return new WeeklyReportInfo(
                                report.getWeekNumber(),
                                report.getWeekStartDate() != null ? report.getWeekStartDate().toString() : null,
                                report.getTasksAssigned(),
                                report.getTasksCompleted(),
                                report.getIssuesRisks(),
                                report.getMentorOverallComment(),
                                report.getAverageScore(),
                                criteriaScores);
        }

        private CriteriaScore mapToCriteriaScore(WeeklyReportDetail detail) {
                return new CriteriaScore(
                                detail.getCriteria() != null ? detail.getCriteria().getName() : null,
                                detail.getScore(),
                                detail.getComment());
        }
}
