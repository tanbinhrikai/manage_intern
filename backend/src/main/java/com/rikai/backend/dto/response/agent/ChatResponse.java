package com.rikai.backend.dto.response.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for AI agent chat endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String message;
    private Instant timestamp;

    public static ChatResponse of(String message) {
        return ChatResponse.builder()
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
