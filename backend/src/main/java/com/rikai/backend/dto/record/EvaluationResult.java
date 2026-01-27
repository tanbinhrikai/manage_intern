package com.rikai.backend.dto.record;

import com.rikai.backend.ai.tools.EvaluationSessionTool;

import java.math.BigDecimal;
import java.util.List;

public record EvaluationResult(
        Integer sessionId,
        String sessionType,
        String evaluationDate,
        BigDecimal finalScore,
        String levelAssessment,
        String conclusion,
        String overallComment,
        String mentorName,
        List<EvaluationScoreResult> scores) {
}
