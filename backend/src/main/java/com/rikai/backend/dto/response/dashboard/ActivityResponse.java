package com.rikai.backend.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityResponse {
    String type; // "new", "evaluation", "warning", "completed", "status_change"
    String text; // HTML formatted text
    Instant timestamp;
    String internName;
    String mentorName;
    Long internId;
    String oldStatus;
    String newStatus;
}
