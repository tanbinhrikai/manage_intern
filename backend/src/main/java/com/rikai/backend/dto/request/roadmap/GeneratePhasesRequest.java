package com.rikai.backend.dto.request.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePhasesRequest {
    private Long positionId;
    private Long batchId;
    private Integer duration;
    private String prompt;
}
