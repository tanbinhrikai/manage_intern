package com.rikai.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentCreationRequest {
    @NotBlank(message = "DEPARTMENT_TITLE_REQUIRED")
    String title;
}
