package com.rikai.backend.service.weeklyreport;

import com.rikai.backend.dto.request.weeklyreport.WeeklyReportCreateDTO;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportUpdateDTO;
import com.rikai.backend.common.PageResponse;
import com.rikai.backend.dto.response.weeklyreport.WeeklyReportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IWeeklyReportService {

    PageResponse<WeeklyReportResponse> getAllReports(Pageable pageable, Long internId);

    WeeklyReportResponse getReportById(Integer id);

    WeeklyReportResponse createReport(WeeklyReportCreateDTO createDTO);

    WeeklyReportResponse updateReport(Integer id, WeeklyReportUpdateDTO updateDTO);

    void deleteReport(Integer id);

    List<WeeklyReportResponse> getReportsByInternId(Long internId);
}
