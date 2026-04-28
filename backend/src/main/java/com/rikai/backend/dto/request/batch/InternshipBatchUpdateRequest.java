package com.rikai.backend.dto.request.batch;

import com.rikai.backend.model.Enum.BatchStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InternshipBatchUpdateRequest {
    String name;
    LocalDate startDate;

    LocalDate endDate;
    String description;

    BatchStatus status;
    String picMentorId;
}