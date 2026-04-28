package com.rikai.backend.dto.response.evaluationsession;

import com.rikai.backend.model.Enum.EvaluationConclusion;
import com.rikai.backend.model.Enum.SessionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationSessionResponse {
    private Integer id;
    private Long internId;
    private String internName;
    private String mentorId;
    private String mentorName;
    private SessionType sessionType;
    private LocalDate evaluationDate;
    private BigDecimal finalScore;
    private String levelAssessment;
    private EvaluationConclusion conclusion;
    private String overallComment;
    private List<EvaluationScoreResponse> scores;
    private Instant createdAt;
    private Instant updatedAt;
}
