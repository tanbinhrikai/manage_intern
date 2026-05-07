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
public class InternUpdateRequest {
    String fullName;
    Long positionId;
    UUID mentorId;
    Long internShipBatchId;
    LocalDate startDate;
    LocalDate endDate;
    InternStatus internStatus;
}
