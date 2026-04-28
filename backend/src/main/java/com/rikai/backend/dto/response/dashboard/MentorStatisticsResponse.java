package com.rikai.backend.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MentorStatisticsResponse {
    long totalInterns;
    long activeInterns;
    long warningInterns;
    long pendingReports;
}
