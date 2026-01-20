package com.rikai.backend.dto.request.evaluation_criteria;

import com.rikai.backend.model.Enum.CriteriaCategory;
import jakarta.validation.constraints.NotBlank;
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
    @NotNull(message = "CRITERIA_CATEGORY_REQUIRED")
    CriteriaCategory category;

    @NotBlank(message = "CRITERIA_NAME_REQUIRED")
    String name;

    String description;

    BigDecimal weight;
}
