package com.rikai.backend.controller;

import com.rikai.backend.common.ApiResponse;
import com.rikai.backend.common.SuccessCode;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportCreateDTO;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportUpdateDTO;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.weeklyreport.WeeklyReportResponse;
import com.rikai.backend.service.weeklyreport.IWeeklyReportService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/weekly-reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeeklyReportController {

    IWeeklyReportService weeklyReportService;

    /**
     * GET /weekly-reports
     * Get all weekly reports (with pagination and optional intern filter)
     * - Admin: can see all reports
     * - Mentor: can only see reports of assigned interns
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<WeeklyReportResponse>> getAllReports(
            @RequestParam(required = false) Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        // sort by weekStartDate desc by default
        PageRequest pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "weekStartDate"));
        PageResponse<WeeklyReportResponse> result = weeklyReportService.getAllReports(pageable, internId);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_WEEKLY_REPORTS_SUCCESSFUL);
    }

    /**
     * GET /weekly-reports/{id}
     * Get weekly report by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<WeeklyReportResponse> getReportById(@PathVariable Integer id) {
        WeeklyReportResponse result = weeklyReportService.getReportById(id);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_WEEKLY_REPORT_SUCCESSFUL);
    }

    /**
     * POST /weekly-reports
     * Create new weekly report
     * - Mentor: can only create for assigned interns
     * - Admin: can create for any intern
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<WeeklyReportResponse> createReport(
            @Valid @RequestBody WeeklyReportCreateDTO createDTO) {
        WeeklyReportResponse result = weeklyReportService.createReport(createDTO);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.CREATE_WEEKLY_REPORT_SUCCESSFUL);
    }

    /**
     * PUT /weekly-reports/{id}
     * Update weekly report
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<WeeklyReportResponse> updateReport(
            @PathVariable Integer id,
            @Valid @RequestBody WeeklyReportUpdateDTO updateDTO) {
        WeeklyReportResponse result = weeklyReportService.updateReport(id, updateDTO);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.UPDATE_WEEKLY_REPORT_SUCCESSFUL);
    }

    /**
     * DELETE /weekly-reports/{id}
     * Delete weekly report
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<Void> deleteReport(@PathVariable Integer id) {
        weeklyReportService.deleteReport(id);
        return ApiResponse.buildSuccessResponse(null, SuccessCode.DELETE_WEEKLY_REPORT_SUCCESSFUL);
    }

    /**
     * GET /intern/{internId}
     * Get all weekly reports for a specific intern
     */
    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ApiResponse<PageResponse<WeeklyReportResponse>> getReportsByInternId(
            @PathVariable Long internId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "weekStartDate"));
        PageResponse<WeeklyReportResponse> result = weeklyReportService.getReportsByInternId(internId, pageRequest);
        return ApiResponse.buildSuccessResponse(result, SuccessCode.GET_ALL_WEEKLY_REPORTS_SUCCESSFUL);
    }
}
