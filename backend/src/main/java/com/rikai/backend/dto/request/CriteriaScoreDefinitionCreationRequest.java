package com.rikai.backend.dto.request;

import com.rikai.backend.model.Enum.ScoreLabel;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaScoreDefinitionCreationRequest {
    @NotNull(message = "CRITERIA_ID_REQUIRED")
    Integer criteriaId;

    ScoreLabel scoreLabel;

    @NotNull(message = "MIN_SCORE_REQUIRED")
    Byte minScore;

    @NotNull(message = "MAX_SCORE_REQUIRED")
    Byte maxScore;

    String description;
}
