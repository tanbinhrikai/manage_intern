package com.rikai.backend.dto.record;

import java.math.BigDecimal;

public record CriteriaScore(
        String criteriaName,
        BigDecimal score,
        String comment) {
}
