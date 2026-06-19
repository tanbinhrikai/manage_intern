package com.rikai.backend.dto.request.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeExpansionRequest {
    private Long nodeId;
    private String prompt;
}
