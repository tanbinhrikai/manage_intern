package com.rikai.backend.service.dashboard;

import com.rikai.backend.dto.response.dashboard.ActivityResponse;
import com.rikai.backend.dto.response.dashboard.ChartDataResponse;
import com.rikai.backend.dto.response.dashboard.MultiSeriesChartResponse;

import java.util.List;

import java.time.LocalDate;
import java.time.YearMonth;

public interface IDashboardService {
    List<ActivityResponse> getRecentActivities(int limit);
    ChartDataResponse getMentorsByDepartment();
    ChartDataResponse getInternsByPosition();
    ChartDataResponse getInternsTrend(int months);
    ChartDataResponse getInternsTrendByDateRange(YearMonth startMonth, YearMonth endMonth);
    ChartDataResponse getInternsTrendByDayRange(LocalDate startDate, LocalDate endDate);
    ChartDataResponse getAverageScoreTrend(int months);
    ChartDataResponse getCompletionRateTrend(int months);
    
    // Multi-series chart methods grouped by position
    MultiSeriesChartResponse getAverageScoreTrendByGroup(int months);
    MultiSeriesChartResponse getCompletionRateTrendByGroup(int months);
    
    // Batch score trend methods
    MultiSeriesChartResponse getBatchScoreTrend();
    MultiSeriesChartResponse getInternScoreTrendByBatch(Long batchId);
}
