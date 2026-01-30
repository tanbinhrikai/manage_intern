package com.rikai.backend.dto.request.learning_plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningPlanCreationRequest {
    @NotNull(message = "INTERN_ID_REQUIRED")
    Long internId;

    @NotBlank(message = "LEARNING_PLAN_TITLE_REQUIRED")
    String title;

    String description;

    List<ModuleRequest> modules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ModuleRequest {
        String title;
        String focusTopic;
        Integer orderIndex;
        List<TaskRequest> tasks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TaskRequest {
        String title;
        String description;
        String resourceLink;
        Integer estimatedMinutes;
        Integer orderIndex;
    }
}
