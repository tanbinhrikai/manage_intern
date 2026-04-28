package com.rikai.backend.ai.dto.response;

public record UserIntentDto(
        boolean isRoadmapRequest,
        String detectedTopic,
        String detectedDuration,
        Double estimatedHours,
        String additionalNotes,
        String conversationalReply
) {}