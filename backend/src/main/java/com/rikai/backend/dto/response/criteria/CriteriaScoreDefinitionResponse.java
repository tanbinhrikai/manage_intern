package com.rikai.backend.dto.response.criteria;

import com.rikai.backend.model.Enum.ScoreLabel;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaScoreDefinitionResponse {
    Long id;
    Long criteriaId;
    ScoreLabel scoreLabel;
    String description;
}
