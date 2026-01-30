package com.rikai.backend.ai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDto {
    private String message;
    private ActionType action;
    private Object data;

    public enum ActionType {
        NORMAL_CHAT,
        SELECT_POSITION,
        SELECT_DURATION,
        DISPLAY_ROADMAP
    }
}