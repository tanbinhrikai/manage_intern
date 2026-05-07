package com.rikai.backend.dto.request.evaluation_criteria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaCreationRequest {
    @NotNull(message = "CRITERIA_GROUP_REQUIRED")
    Long groupId;

    @NotBlank(message = "CRITERIA_NAME_REQUIRED")
    String name;

    String description;

    @Builder.Default
    BigDecimal weight = BigDecimal.ONE;
    Long parentId;
    @Min(value = 1, message = "DISPLAY_ORDER_INVALID")
    Integer displayOrder;
    Boolean isActive;
}
