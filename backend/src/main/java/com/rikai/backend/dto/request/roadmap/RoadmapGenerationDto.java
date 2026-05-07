package com.rikai.backend.dto.request.roadmap;

import java.util.List;

public record RoadmapGenerationDto(
        String title,
        String description,
        String type,
        String pass_condition,
        String learning_outcome,
        Double estimated_hours,
        String assessment_method,
        String difficulty,
        List<String> tags,
        List<RoadmapGenerationDto> children
) {}