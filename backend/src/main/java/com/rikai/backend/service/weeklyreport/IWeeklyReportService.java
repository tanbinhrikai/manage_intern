package com.rikai.backend.service.weeklyreport;

import com.rikai.backend.dto.request.weeklyreport.WeeklyReportCreateDTO;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportUpdateDTO;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.weeklyreport.WeeklyReportResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IWeeklyReportService {

    PageResponse<WeeklyReportResponse> getAllReports(PageRequest pageRequest, Long internId);

    WeeklyReportResponse getReportById(Integer id);

    WeeklyReportResponse createReport(WeeklyReportCreateDTO createDTO);

    WeeklyReportResponse updateReport(Integer id, WeeklyReportUpdateDTO updateDTO);

    void deleteReport(Integer id);

    PageResponse<WeeklyReportResponse> getReportsByInternId(Long internId, PageRequest pageRequest);

    /**
     * Get all weekly reports created by the current mentor with optional filtering.
     *
     * @param pageRequest pagination information
     * @param internId    optional intern ID to filter by
     * @param startDate   optional start date for filtering
     * @param endDate     optional end date for filtering
     * @return paginated list of weekly reports
     */
    PageResponse<WeeklyReportResponse> getMyReports(PageRequest pageRequest, Long internId, LocalDate startDate,
            LocalDate endDate);
}
