package com.rikai.backend.dto.response.evaluation_criteria;

import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;
import com.rikai.backend.model.Enum.CriteriaCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaResponse {
    Long id;
    CriteriaCategory category;
    String categoryDisplayName;
    String categoryDescription;
    String name;
    String description;
    BigDecimal weight;
    Integer displayOrder;
    Boolean isActive;
    Long parentId;
    String parentName;
    List<EvaluationCriteriaResponse> children;
    List<CriteriaScoreDefinitionResponse> scoreDefinitions;
}
