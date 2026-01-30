package com.rikai.backend.dto.request.learning_plan;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningPlanUpdateRequest {
    String title;
    String description;
    List<ModuleRequest> modules;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ModuleRequest {
        Long id;
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
        Long id;
        String title;
        String description;
        String resourceLink;
        Integer estimatedMinutes;
        String status;
        Integer orderIndex;
    }
}
