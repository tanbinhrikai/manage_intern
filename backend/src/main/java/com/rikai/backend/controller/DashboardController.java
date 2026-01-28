package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.response.dashboard.ActivityResponse;
import com.rikai.backend.dto.response.dashboard.ChartDataResponse;
import com.rikai.backend.dto.response.dashboard.MultiSeriesChartResponse;
import com.rikai.backend.service.dashboard.IDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardController {
    
    IDashboardService dashboardService;

    /**
     * Endpoint to retrieve recent activities for admin and mentor roles.
     *
     * @param limit the maximum number of recent activities to retrieve (default is 10)
     * @return ApiResponse containing a list of recent activities
     */
    
    @GetMapping("/recent-activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<List<ActivityResponse>> getRecentActivities(
            @RequestParam(defaultValue = "100") int limit) {
        List<ActivityResponse> activities = dashboardService.getRecentActivities(limit);
        return ApiResponse.buildSuccessResponse(activities, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/mentors-by-department")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<ChartDataResponse> getMentorsByDepartment() {
        ChartDataResponse data = dashboardService.getMentorsByDepartment();
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/interns-by-position")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<ChartDataResponse> getInternsByPosition() {
        ChartDataResponse data = dashboardService.getInternsByPosition();
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/interns-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<ChartDataResponse> getInternsTrend(
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth startMonth,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth endMonth,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        ChartDataResponse data;
        if (startDate != null && endDate != null) {
            data = dashboardService.getInternsTrendByDayRange(startDate, endDate);
        } else if (startMonth != null && endMonth != null) {
            data = dashboardService.getInternsTrendByDateRange(startMonth, endMonth);
        } else {
            data = dashboardService.getInternsTrend(months);
        }
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/average-score-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<ChartDataResponse> getAverageScoreTrend(
            @RequestParam(defaultValue = "6") int months) {
        ChartDataResponse data = dashboardService.getAverageScoreTrend(months);
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/completion-rate-trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<ChartDataResponse> getCompletionRateTrend(
            @RequestParam(defaultValue = "6") int months) {
        ChartDataResponse data = dashboardService.getCompletionRateTrend(months);
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/average-score-trend-by-group")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<MultiSeriesChartResponse> getAverageScoreTrendByGroup(
            @RequestParam(defaultValue = "6") int months) {
        MultiSeriesChartResponse data = dashboardService.getAverageScoreTrendByGroup(months);
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
    
    @GetMapping("/chart/completion-rate-trend-by-group")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<MultiSeriesChartResponse> getCompletionRateTrendByGroup(
            @RequestParam(defaultValue = "6") int months) {
        MultiSeriesChartResponse data = dashboardService.getCompletionRateTrendByGroup(months);
        return ApiResponse.buildSuccessResponse(data, SuccessCode.GET_RECENT_ACTIVITIES_SUCCESSFUL);
    }
}
