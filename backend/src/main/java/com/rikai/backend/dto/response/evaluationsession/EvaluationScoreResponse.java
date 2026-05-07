package com.rikai.backend.dto.response.evaluationsession;

import com.rikai.backend.model.EvaluationSession;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationScoreResponse {
    private Integer id;
    private Long criteriaId;
    private String criteriaName;
    private String criteriaCategory;
    private BigDecimal score;
    private String comment;

    public EvaluationSessionResponse toResponse(EvaluationSession session) {
        List<EvaluationScoreResponse> scoreResponses = new ArrayList<>();
        if (session.getScores() != null) {
            scoreResponses = session.getScores().stream()
                    .map(es -> EvaluationScoreResponse.builder()
                            .id(es.getId())
                            .criteriaId(es.getCriteria().getId())
                            .criteriaName(es.getCriteria().getName())
                            .criteriaCategory(es.getCriteria().getGroup().getName())
                            .score(es.getScore())
                            .comment(es.getComment())
                            .build())
                    .collect(Collectors.toList());
        }

        return EvaluationSessionResponse.builder()
                .id(session.getId())
                .internId(session.getIntern().getId())
                .internName(session.getIntern().getFullName())
                .mentorId(session.getMentor().getId().toString())
                .mentorName(session.getMentor().getFullName())
                .sessionType(session.getSessionType())
                .evaluationDate(session.getEvaluationDate())
                .finalScore(session.getFinalScore())
                .levelAssessment(session.getLevelAssessment())
                .conclusion(session.getConclusion())
                .overallComment(session.getOverallComment())
                .scores(scoreResponses)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
