package com.rikai.backend.dto.response.roadmap;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoadmapListResponse {
    List<RoadmapNodeResponse> items;
    int total;
}
