package com.rikai.backend.dto.record;

import java.util.List;

public record WeeklyReportResult(
        String internName,
        List<WeeklyReportInfo> reports,
        Double overallAverageScore) {
}