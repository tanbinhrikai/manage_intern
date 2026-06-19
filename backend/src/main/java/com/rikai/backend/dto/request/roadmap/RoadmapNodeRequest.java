package com.rikai.backend.dto.request.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNodeRequest {
    private Long roadmapId;
    private Long parentId;
    private String title;
    private String description;
    private Double estimatedHours;
}
