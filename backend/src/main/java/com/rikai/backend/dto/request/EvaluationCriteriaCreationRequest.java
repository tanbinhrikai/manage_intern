package com.rikai.backend.dto.request;

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
public class EvaluationCriteriaCreationRequest {
    @NotNull(message = "CRITERIA_CATEGORY_REQUIRED")
    CriteriaCategory category;

    @NotBlank(message = "CRITERIA_NAME_REQUIRED")
    String name;

    String description;

    @Builder.Default
    BigDecimal weight = BigDecimal.ONE;
}
