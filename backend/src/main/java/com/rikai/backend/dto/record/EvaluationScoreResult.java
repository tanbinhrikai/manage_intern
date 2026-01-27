package com.rikai.backend.dto.record;

import java.math.BigDecimal;

public record EvaluationScoreResult(
        String criteriaName,
        BigDecimal score,
        String comment) {
}
