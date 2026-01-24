package com.rikai.backend.dto.response.intern;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternAnalysisResponse {
    long totalInterns;
    long totalMentors;
    long activeInterns;
    long warningInterns;
    long droppedInterns;
    long completedInterns;
}
