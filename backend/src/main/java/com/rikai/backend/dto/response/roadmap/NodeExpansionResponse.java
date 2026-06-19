package com.rikai.backend.dto.response.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeExpansionResponse {
    private Long parentId;
    private List<RoadmapNodeResponse> children;
}
