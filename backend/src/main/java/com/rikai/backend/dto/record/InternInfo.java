package com.rikai.backend.dto.record;

public record InternInfo(
        Long id,
        String fullName,
        String email,
        String position,
        String status,
        String startDate,
        String endDate) {
}
