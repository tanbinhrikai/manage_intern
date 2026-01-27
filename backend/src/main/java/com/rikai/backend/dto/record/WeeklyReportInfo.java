package com.rikai.backend.dto.record;

import java.math.BigDecimal;
import java.util.List;

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
