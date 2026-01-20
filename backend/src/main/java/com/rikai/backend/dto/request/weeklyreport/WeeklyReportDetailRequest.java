package com.rikai.backend.dto.request.weeklyreport;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WeeklyReportDetailRequest {
    @NotNull(message = "CRITERIA_ID_REQUIRED")
    private Long criteriaId;

    @Min(value = 1, message = "INVALID_SCORE")
    @Max(value = 10, message = "INVALID_SCORE")
    private Byte score;

    private String comment;
}
