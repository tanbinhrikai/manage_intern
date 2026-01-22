package com.rikai.backend.dto.request.evaluationsession;

import com.rikai.backend.model.Enum.SessionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EvaluationSessionCreateRequest {
    @NotNull(message = "INTERN_ID_REQUIRED")
    private Long internId;

    @NotNull(message = "SESSION_TYPE_REQUIRED")
    private SessionType sessionType;

    @NotNull(message = "EVALUATION_DATE_REQUIRED")
    private LocalDate evaluationDate;

    private String overallComment;

    private List<EvaluationScoreRequest> scores; // Optional: for manual override
}
