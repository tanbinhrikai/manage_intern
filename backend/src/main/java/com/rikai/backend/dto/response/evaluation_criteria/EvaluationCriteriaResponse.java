package com.rikai.backend.dto.response.evaluation_criteria;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.dto.response.criteria.CriteriaScoreDefinitionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluationCriteriaResponse {
    Long id;
    Long groupId;
    String groupName;
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
