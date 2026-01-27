package com.rikai.backend.dto.response.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class CoursePlanDTO {
    String title;
    String description;
    List<ModuleDTO> modules;

    @Data
    public static class ModuleDTO {
        String title;
        @JsonProperty("focus_topic")
        String focusTopic;
        List<TaskDTO> tasks;
    }

    @Data
    public static class TaskDTO {
        String title;
        String description;
        @JsonProperty("resource_link")
        String resourceLink;
        @JsonProperty("estimated_minutes")
        Integer estimatedMinutes;

        @JsonProperty("sub_plan")
        CoursePlanDTO subPlan;
    }
}