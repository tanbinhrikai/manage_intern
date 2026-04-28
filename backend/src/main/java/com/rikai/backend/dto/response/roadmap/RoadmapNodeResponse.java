package com.rikai.backend.dto.response.roadmap;

import com.rikai.backend.model.Enum.DifficultyLevel;
import com.rikai.backend.model.Enum.NodeType;
import com.rikai.backend.model.RoadmapNode;
import com.rikai.backend.model.Tag;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoadmapNodeResponse {
    Long id;
    String title;
    String description;
    NodeType nodeType;
    DifficultyLevel difficulty;
    Double estimatedHours;
    Integer orderIndex;
    String assessmentMethod;
    String passCondition;
    String learningOutcome;
    Boolean isExpanded;
    Instant createdAt;
    Instant updatedAt;
    Set<String> tags;
    List<RoadmapNodeResponse> children;

    public static RoadmapNodeResponse toRoadmapResponse(RoadmapNode node) {
        return RoadmapNodeResponse.builder()
                .id(node.getId())
                .title(node.getTitle())
                .description(node.getDescription())
                .nodeType(node.getNodeType())
                .difficulty(node.getDifficulty())
                .estimatedHours(node.getEstimatedHours())
                .orderIndex(node.getOrderIndex())
                .assessmentMethod(node.getAssessmentMethod())
                .passCondition(node.getPassCondition())
                .learningOutcome(node.getLearningOutcome())
                .isExpanded(node.getIsExpanded())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .tags(node.getTags() != null ?
                        node.getTags().stream().map(Tag::getName)
                                .collect(java.util.stream.Collectors.toSet()) : null)
                .children(node.getChildren() != null ? node.getChildren()
                        .stream().map(RoadmapNodeResponse::toRoadmapResponse).toList() : null)
                .build();
    }
}
