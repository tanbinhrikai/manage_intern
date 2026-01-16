package com.rikai.backend.dto.response;

import com.rikai.backend.model.Enum.CriteriaCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaResponse {
    Integer id;
    CriteriaCategory category;
    String name;
    String description;
    BigDecimal weight;
    Set<CriteriaScoreDefinitionResponse> scoreDefinitions;
}
