package com.rikai.backend.dto.response.learning_plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningPlanResponse {
    Long id;
    Long internId;
    String internName;
    String title;
    String description;
    String generatedFromPrompt;
    List<PlanModuleResponse> modules;
    Instant createdAt;
}
