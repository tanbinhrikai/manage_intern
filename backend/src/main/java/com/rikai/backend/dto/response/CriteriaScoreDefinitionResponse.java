package com.rikai.backend.dto.response;

import com.rikai.backend.model.Enum.ScoreLabel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaScoreDefinitionResponse {
    Integer id;
    Integer criteriaId;
    ScoreLabel scoreLabel;
    String description;
    Instant createdAt;
}
