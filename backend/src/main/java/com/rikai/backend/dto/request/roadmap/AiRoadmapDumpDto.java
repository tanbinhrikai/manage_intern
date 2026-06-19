package com.rikai.backend.dto.request.roadmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO bọc toàn bộ "kho" dữ liệu dump, gồm responses cho từng cấp độ NodeType.
 * Map trực tiếp từ file ai_roadmap_dump.json.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRoadmapDumpDto {
    @JsonProperty("PHASE_RESPONSE")
    private List<AiNodeResponseDto> PHASE_RESPONSE;

    @JsonProperty("MODULE_RESPONSE")
    private List<AiNodeResponseDto> MODULE_RESPONSE;

    @JsonProperty("LESSON_RESPONSE")
    private List<AiNodeResponseDto> LESSON_RESPONSE;

    @JsonProperty("TOPIC_RESPONSE")
    private List<AiNodeResponseDto> TOPIC_RESPONSE;

    @JsonProperty("TASK_RESPONSE")
    private List<AiNodeResponseDto> TASK_RESPONSE;
}
