package com.rikai.backend.dto.response;

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
    
    // Category info
    CriteriaCategory category;
    String categoryDisplayName;
    String categoryDescription;
    
    // Criteria info
    String name;
    String description;
    BigDecimal weight;
    Integer displayOrder;
    Boolean isActive;
    
    // Hierarchical structure
    Long parentId;
    String parentName;
    List<EvaluationCriteriaResponse> children;
    
    // Score definitions (chỉ cho sub-criteria)
    List<CriteriaScoreDefinitionResponse> scoreDefinitions;
}
