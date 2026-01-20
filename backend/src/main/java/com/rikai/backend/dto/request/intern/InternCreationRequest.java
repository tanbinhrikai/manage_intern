package com.rikai.backend.dto.request.intern;

import com.rikai.backend.common.InternStatus;
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
public class InternCreationRequest {
    @NotBlank(message = "INVALID_FULLNAME")
    String fullName;

    @NotNull(message = "JOB_POSITION_REQUIRED")
    Long positionId;

    @NotNull(message = "MENTOR_REQUIRED")
    UUID mentorId;

    @NotNull(message = "START_DATE_REQUIRED")
    LocalDate startDate;

    @NotNull(message = "END_DATE_REQUIRED")
    LocalDate endDate;
}
