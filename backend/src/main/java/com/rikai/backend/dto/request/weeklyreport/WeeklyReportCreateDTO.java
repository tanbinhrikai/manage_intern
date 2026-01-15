package com.rikai.backend.dto.request.weeklyreport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WeeklyReportCreateDTO {
    
    @NotNull(message = "INTERN_ID_REQUIRED")
    private Integer internId;
    
    @NotNull(message = "WEEK_START_DATE_REQUIRED")
    private LocalDate weekStartDate;
    
    private String tasksAssigned;
    
    private String tasksCompleted;
    
    private String outputQuality;
    
    @Min(value = 1, message = "INVALID_SCORE")
    @Max(value = 10, message = "INVALID_SCORE")
    private Byte proactivityScore;
    
    @Min(value = 1, message = "INVALID_SCORE")
    @Max(value = 10, message = "INVALID_SCORE")
    private Byte progressScore;
    
    private String issuesRisks;
    
    private String mentorOverallComment;
}
