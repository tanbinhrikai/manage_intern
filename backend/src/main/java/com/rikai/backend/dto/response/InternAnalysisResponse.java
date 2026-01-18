package com.rikai.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InternAnalysisResponse {
    long totalInterns;
    long totalMentors;
    long activeInterns;
    long warningInterns;
    long droppedInterns;
    long completedInterns;
}
