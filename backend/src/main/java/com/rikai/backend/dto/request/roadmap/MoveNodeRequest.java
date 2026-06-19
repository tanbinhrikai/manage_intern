package com.rikai.backend.dto.request.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveNodeRequest {
    private Long targetNodeId;
    private String dropType; // "inner", "before", "after"
}
