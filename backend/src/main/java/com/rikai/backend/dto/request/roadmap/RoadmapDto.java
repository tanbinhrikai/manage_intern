package com.rikai.backend.dto.request.roadmap;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapDto {
    private Long id;
    private Long roadmapId;
    private String title;
    private String description;
    private Integer durationMonth;
    private Long positionId;
    private Long batchId;
    private String status;
    private List<RoadmapNodeDto> nodes;
}
