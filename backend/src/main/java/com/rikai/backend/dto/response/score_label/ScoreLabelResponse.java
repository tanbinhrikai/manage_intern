package com.rikai.backend.dto.response.score_label;

import com.rikai.backend.model.Enum.ScoreLabel;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScoreLabelResponse {
    ScoreLabel value;
    String displayName;
    String description;
    Integer minScore;
    Integer maxScore;
}
