package com.rikai.backend.dto.response.weeklyreport;

import com.rikai.backend.model.WeeklyReport;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyReportResponse {
    private Integer id;
    private Integer internId;
    private String internName;
    private String mentorId;
    private String mentorName;
    private Integer weekNumber;
    private LocalDate weekStartDate;
    private String tasksAssigned;
    private String tasksCompleted;
    private String outputQuality;
    private Byte proactivityScore;
    private Byte progressScore;
    private String issuesRisks;
    private String mentorOverallComment;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    
    public static WeeklyReportResponse fromWeeklyReport(WeeklyReport report) {
        if (report == null) {
            return null;
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
                .outputQuality(report.getOutputQuality())
                .proactivityScore(report.getProactivityScore())
                .progressScore(report.getProgressScore())
                .issuesRisks(report.getIssuesRisks())
                .mentorOverallComment(report.getMentorOverallComment())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
