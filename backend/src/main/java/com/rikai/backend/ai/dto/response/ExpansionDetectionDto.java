package com.rikai.backend.ai.dto.response;

/**
 * DTO for detecting expansion requests from user messages.
 */
public record ExpansionDetectionDto(
        boolean isExpansionRequest,
        String targetNodeTitle,
        String expansionDepth,
        String reply
) {}
