package com.rikai.backend.dto.request.weeklyreport;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WeeklyReportUpdateDTO {

    private LocalDate weekStartDate;

    private String tasksAssigned;

    private String tasksCompleted;

    private String issuesRisks;

    private String mentorOverallComment;

    private List<WeeklyReportDetailRequest> details;
}
