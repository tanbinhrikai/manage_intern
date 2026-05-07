package com.rikai.backend.ai.dto.request;

public record UpdateNodeRequest(
        String targetNodeName,
        String newTitle,
        String newDescription
) {
}
