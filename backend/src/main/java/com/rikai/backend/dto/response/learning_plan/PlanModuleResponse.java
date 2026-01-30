package com.rikai.backend.dto.response.learning_plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanModuleResponse {
    Long id;
    String title;
    String focusTopic;
    Integer orderIndex;
    List<PlanTaskResponse> tasks;
}
