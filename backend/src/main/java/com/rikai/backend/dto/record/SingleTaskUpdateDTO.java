package com.rikai.backend.dto.record;

public record SingleTaskUpdateDTO(
        String title,
        String description,
        String resource_link,
        Integer estimated_minutes
) {}
