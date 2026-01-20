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
    Long criteriaId;

    @NotNull(message = "SCORE_LABEL_REQUIRED")
    ScoreLabel scoreLabel;

    String description;
}
