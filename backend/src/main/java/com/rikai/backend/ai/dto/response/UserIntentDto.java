package com.rikai.backend.ai.dto.response;

public record UserIntentDto(
        boolean isRoadmapRequest,
        String detectedTopic,
        String detectedDuration,
        String additionalNotes,
        String conversationalReply
) {}