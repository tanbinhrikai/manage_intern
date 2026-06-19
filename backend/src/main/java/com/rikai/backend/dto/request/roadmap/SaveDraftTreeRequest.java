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
public class SaveDraftTreeRequest {
    private Long id;
    private String title;
    private String description;
    private Integer durationMonth;
    private Long positionId;
    private Long batchId;
    private Boolean publish;
    private List<RoadmapNodeDto> nodes;
}
