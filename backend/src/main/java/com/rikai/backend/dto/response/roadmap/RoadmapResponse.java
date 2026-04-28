package com.rikai.backend.dto.response.roadmap;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoadmapResponse {
    Long id;
    String title;
    String description;
    Instant createdAt;
    Instant updatedAt;
}
