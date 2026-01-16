package com.rikai.backend.dto.response.weeklyreport;

import com.rikai.backend.model.WeeklyReport;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyReportResponse {
    private Integer id;
    private Long internId;
    private String internName;
    private String mentorId;
    private String mentorName;
    private Integer weekNumber;
    private LocalDate weekStartDate;
    private String tasksAssigned;
    private String tasksCompleted;
    private String issuesRisks;
    private String mentorOverallComment;
    private String status;
    private BigDecimal averageScore;
    private List<WeeklyReportDetailResponse> details;
    private Instant createdAt;
    private Instant updatedAt;

    public static WeeklyReportResponse fromWeeklyReport(WeeklyReport report) {
        if (report == null) {
            return null;
        }

        List<WeeklyReportDetailResponse> detailResponses = new ArrayList<>();
        if (report.getDetails() != null) {
            detailResponses = report.getDetails().stream()
                    .map(detail -> WeeklyReportDetailResponse.builder()
                            .id(detail.getId())
                            .criteriaId(detail.getCriteria().getId())
                            .criteriaName(
                                    detail.getCriteria() != null ? detail.getCriteria().getName() : null)
                            .score(detail.getScore())
                            .comment(detail.getComment())
                            .build())
                    .collect(Collectors.toList());
        }

        return WeeklyReportResponse.builder()
                .id(report.getId())
                .internId(report.getIntern() != null ? report.getIntern().getId() : null)
                .internName(report.getIntern() != null ? report.getIntern().getFullName() : null)
                .mentorId(report.getMentor() != null ? report.getMentor().getId().toString() : null)
                .mentorName(report.getMentor() != null ? report.getMentor().getFullName() : null)
                .weekNumber(report.getWeekNumber())
                .weekStartDate(report.getWeekStartDate())
                .tasksAssigned(report.getTasksAssigned())
                .tasksCompleted(report.getTasksCompleted())
                .averageScore(report.getAverageScore())
                .issuesRisks(report.getIssuesRisks())
                .mentorOverallComment(report.getMentorOverallComment())
                .status(report.getStatus())
                .details(detailResponses)
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
