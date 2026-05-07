package com.rikai.backend.ai.tool;

public class CallTool {
    public record PositionQuery(String keyword) {}

    public record PositionResult(Long id, String title, String description) {}
}
