package com.rikai.backend.service.weeklyreport;

import com.rikai.backend.dto.request.weeklyreport.WeeklyReportCreateDTO;
import com.rikai.backend.dto.request.weeklyreport.WeeklyReportUpdateDTO;
import com.rikai.backend.dto.response.PageResponse;
import com.rikai.backend.dto.response.weeklyreport.WeeklyReportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IWeeklyReportService {
    
    PageResponse<WeeklyReportResponse> getAllReports(Pageable pageable, Integer internId);
    
    WeeklyReportResponse getReportById(Integer id);
    
    WeeklyReportResponse createReport(WeeklyReportCreateDTO createDTO);
    
    WeeklyReportResponse updateReport(Integer id, WeeklyReportUpdateDTO updateDTO);
    
    void deleteReport(Integer id);
    
    List<WeeklyReportResponse> getReportsByInternId(Integer internId);
}
