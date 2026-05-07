package com.rikai.backend.dto.request.criteria_group;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CriteriaGroupCreationRequest {

    @NotBlank(message = "GROUP_NAME_REQUIRED")
    private String name;

    @NotNull(message = "DISPLAY_ORDER_REQUIRED")
    @Min(value = 1, message = "INVALID_DISPLAY_ORDER")
    private Integer displayOrder;
}
