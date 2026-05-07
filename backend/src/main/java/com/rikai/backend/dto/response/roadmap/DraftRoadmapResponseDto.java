package com.rikai.backend.dto.response.roadmap;

import com.rikai.backend.model.RoadmapNode;

/**
 * Response DTO for draft roadmap operations.
 */
public record DraftRoadmapResponseDto(
        String sessionId,
        String message,
        RoadmapNode roadmapTree,
        ActionType actionType
) {
    public enum ActionType {
        OUTLINE_GENERATED,
        NODE_EXPANDED,
        CONFIRMATION_REQUIRED,
        ERROR
    }
}
