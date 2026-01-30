package com.rikai.backend.dto.response.learning_plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanTaskResponse {
    Long id;
    String title;
    String description;
    String resourceLink;
    Integer estimatedMinutes;
    String status;
    Integer orderIndex;
    LearningPlanResponse subPlan;
}
