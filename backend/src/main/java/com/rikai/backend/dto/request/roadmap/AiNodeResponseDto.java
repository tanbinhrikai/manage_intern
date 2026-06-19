package com.rikai.backend.dto.request.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO mô phỏng cấu trúc JSON mà AI sẽ trả về cho mỗi node.
 * Khi tích hợp AI thật, chỉ cần parse response của AI sang DTO này.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiNodeResponseDto {
    private String title;
    private String description;
    private Double estimatedHours;
    private String difficulty;
    private String learningOutcome;
    private String passCondition;
    private String assessmentMethod;
}
