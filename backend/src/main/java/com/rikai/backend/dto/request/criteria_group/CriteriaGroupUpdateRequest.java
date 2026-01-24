package com.rikai.backend.dto.request.criteria_group;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CriteriaGroupUpdateRequest {

    private String name;

    @Min(value = 1, message = "INVALID_DISPLAY_ORDER")
    private Integer displayOrder;
}