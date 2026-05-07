package com.rikai.backend.dto.request.evaluationsession;

import com.rikai.backend.model.Enum.EvaluationConclusion;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EvaluationSessionUpdateRequest {
    private LocalDate evaluationDate;

    private BigDecimal finalScore; // Manual override

    private String levelAssessment; // Manual override

    private EvaluationConclusion conclusion; // Manual override

    private String overallComment;

    private List<EvaluationScoreRequest> scores; // Update scores
}
