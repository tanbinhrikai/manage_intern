package com.rikai.backend.dto.request.weeklyreport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyReportCreateDTO {

    @NotNull(message = "INTERN_ID_REQUIRED")
    private Long internId;

    @NotNull(message = "WEEK_START_DATE_REQUIRED")
    private LocalDate weekStartDate;

    private String tasksAssigned;

    private String tasksCompleted;

    private String issuesRisks;

    private String mentorOverallComment;

    private List<WeeklyReportDetailRequest> details;
}
