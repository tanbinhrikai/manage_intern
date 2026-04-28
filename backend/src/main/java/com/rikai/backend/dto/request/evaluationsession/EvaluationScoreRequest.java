package com.rikai.backend.dto.request.evaluationsession;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluationScoreRequest {
    @NotNull(message = "CRITERIA_ID_REQUIRED")
    private Long criteriaId;

    @Min(value = 1, message = "INVALID_SCORE")
    @Max(value = 10, message = "INVALID_SCORE")
    private BigDecimal score;

    private String comment;
}
