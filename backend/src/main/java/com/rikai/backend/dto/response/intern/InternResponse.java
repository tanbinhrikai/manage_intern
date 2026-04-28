package com.rikai.backend.dto.response.intern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.common.InternStatus;
import com.rikai.backend.dto.response.batch.InternshipBatchResponse;
import com.rikai.backend.dto.response.mentor.MentorResponse;
import com.rikai.backend.dto.response.position.PositionResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternResponse {
    Long id;
    String fullName;
    PositionResponse position;
    InternshipBatchResponse internshipBatch;
    MentorResponse mentor;
    LocalDate startDate;
    LocalDate endDate;
    InternStatus internStatus;
    Instant createdAt;
    Instant updatedAt;
}
