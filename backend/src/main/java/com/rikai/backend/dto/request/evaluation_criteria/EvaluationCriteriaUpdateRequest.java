package com.rikai.backend.dto.request.evaluation_criteria;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvaluationCriteriaUpdateRequest {
    Long groupId;
    String name;
    String description;
    BigDecimal weight;
    Long parentId;
    @Min(value = 1, message = "DISPLAY_ORDER_INVALID")
    Integer displayOrder;
    Boolean isActive;
}
