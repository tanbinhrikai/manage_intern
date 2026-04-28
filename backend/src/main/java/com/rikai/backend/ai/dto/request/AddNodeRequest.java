package com.rikai.backend.ai.dto.request;

public record AddNodeRequest(
        String targetParentName,
        String newNodeTitle,
        String nodeType,
        String description
) {
}
