package com.rikai.backend.dto.request.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InternshipBatchCreationRequest {
    @NotBlank(message = "Batch name is required")
    String name;

    @NotNull(message = "Start date is required")
    LocalDate startDate;

    LocalDate endDate;
    String description;
}