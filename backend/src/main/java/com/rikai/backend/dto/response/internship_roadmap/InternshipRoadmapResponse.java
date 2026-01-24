package com.rikai.backend.dto.response.internship_roadmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.dto.response.position.PositionResponse;
import com.rikai.backend.model.Enum.RoadmapStage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternshipRoadmapResponse {
    Long id;
    PositionResponse position;
    RoadmapStage stageName;
    Integer stageOrder;
    String description;
    Integer durationWeeks;
    String expectedOutcomes;
    Instant createdAt;
}
