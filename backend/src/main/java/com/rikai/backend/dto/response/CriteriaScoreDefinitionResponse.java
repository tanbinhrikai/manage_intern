package com.rikai.backend.dto.response;

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
    
    // Score label info
    ScoreLabel scoreLabel;
    String scoreLabelDisplayName;
    String scoreLabelDescription;
    Integer minScore;
    Integer maxScore;
    
    String description;
}
