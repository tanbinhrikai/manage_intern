package com.rikai.backend.dto.response.roadmap;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoadmapResponse {
    private Long id;
    private Long roadmapId;
    private String title;
    private String description;
    private Integer durationMonth;
    private Long positionId;
    private Long batchId;
    private String status;
    private List<RoadmapNodeResponse> nodes;
    Instant createdAt;
    Instant updatedAt;
}
