package com.rikai.backend.dto.record;

public record InternProfileResult(
        Long id,
        String fullName,
        String email,
        String phone,
        String positionName,
        String mentorName,
        String mentorEmail,
        String batchName,
        String status,
        String startDate,
        String endDate,
        String offerStatus) {
}