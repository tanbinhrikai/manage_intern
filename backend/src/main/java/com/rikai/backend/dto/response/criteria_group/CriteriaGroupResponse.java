package com.rikai.backend.dto.response.criteria_group;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.dto.response.evaluation_criteria.EvaluationCriteriaResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CriteriaGroupResponse {
    Long id;
    String name;
    Integer displayOrder;
    List<EvaluationCriteriaResponse> mainCriteria;
}