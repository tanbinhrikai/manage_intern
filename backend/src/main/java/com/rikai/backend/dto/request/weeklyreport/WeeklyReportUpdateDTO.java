package com.rikai.backend.dto.request.weeklyreport;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WeeklyReportUpdateDTO {
    
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
    
    private String status;
}
