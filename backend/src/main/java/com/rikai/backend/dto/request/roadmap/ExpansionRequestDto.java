package com.rikai.backend.dto.request.roadmap;

import com.rikai.backend.model.Enum.ExpansionDepth;

/**
 * Request DTO for expanding a specific node in a draft roadmap.
 */
public record ExpansionRequestDto(
        String sessionId,
        Long nodeId,
        String nodeTitle, // Optional: if user specifies by name
        ExpansionDepth depth
) {}
