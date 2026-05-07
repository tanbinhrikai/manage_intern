package com.rikai.backend.dto.response.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.dto.response.department.DepartmentResponse;
import com.rikai.backend.dto.response.intern.InternResponse;
import com.rikai.backend.dto.response.internship_roadmap.InternshipRoadmapResponse;
import com.rikai.backend.dto.response.user.UserResponse;
import com.rikai.backend.model.Enum.BatchStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternshipBatchResponse {
    Long id;
    String name;
    LocalDate startDate;
    LocalDate endDate;

    String description;

    BatchStatus status;

    Instant createdAt;

    Long internCount;

    List<InternResponse> internResponses;
}
