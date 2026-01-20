package com.rikai.backend.dto.response.criteria;

import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import com.rikai.backend.model.Enum.CriteriaCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CriteriaCategoryResponse {
    CriteriaCategory category;
    String displayName;
    String description;
    List<EvaluationCriteriaResponse> mainCriteria;
}
