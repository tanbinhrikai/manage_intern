package com.rikai.backend.model;

import com.rikai.backend.model.Enum.RoadmapStage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "internship_roadmaps")
public class InternshipRoadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false)
    Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_name", nullable = false)
    RoadmapStage stageName;

    @Column(name = "stage_order")
    @Builder.Default
    Integer stageOrder = 0;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "duration_weeks")
    Integer durationWeeks;

    @Column(name = "expected_outcomes", columnDefinition = "TEXT")
    String expectedOutcomes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
