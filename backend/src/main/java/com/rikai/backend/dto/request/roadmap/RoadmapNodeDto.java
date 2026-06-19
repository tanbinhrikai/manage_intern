package com.rikai.backend.dto.request.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapNodeDto {
    private Long id;
    private String title;
    private String description;
    private String nodeType;
    private Double estimatedHours;
    private Integer orderIndex;
    private String difficulty;
    private String passCondition;
    private String learningOutcome;
    private String assessmentMethod;
    private List<String> tags;
    private List<RoadmapNodeDto> children;
}
