package com.rikai.backend.dto.request.agent_ai;

import com.rikai.backend.model.RoadmapNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DraftRoadmap {
    private final String sessionId;
    private RoadmapNode rootNode;
    private final Long positionId;
    private final Long batchId;
    private final String duration;
    private Instant lastModified;
    private final String additionalNotes;
}