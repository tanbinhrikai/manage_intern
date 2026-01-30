package com.rikai.backend.dto.record;

import java.util.List;

public record MentorInternsResult(
        String mentorName,
        String mentorEmail,
        String department,
        List<InternInfo> interns) {
}
