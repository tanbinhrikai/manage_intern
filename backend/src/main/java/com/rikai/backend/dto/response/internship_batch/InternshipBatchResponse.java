package com.rikai.backend.dto.response.internship_batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InternshipBatchResponse {
    Long id;
    String name;
    LocalDate startDate;
    LocalDate endDate;
    String description;
    @CreationTimestamp
    Instant createdAt;
}
