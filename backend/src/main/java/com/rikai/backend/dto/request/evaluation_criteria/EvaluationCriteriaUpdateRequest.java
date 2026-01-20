package com.rikai.backend.dto.request.evaluation_criteria;

import com.rikai.backend.model.Enum.CriteriaCategory;
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
public class EvaluationCriteriaUpdateRequest {
    CriteriaCategory category;
    String name;
    String description;
    BigDecimal weight;
    Long parentId;
    @Min(value = 0, message = "DISPLAY_ORDER_INVALID")
    Integer displayOrder;
    Boolean isActive;
}
