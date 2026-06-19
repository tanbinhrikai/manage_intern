package com.rikai.backend.model;

import com.rikai.backend.model.Enum.RoadmapStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "roadmaps")
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    InternshipBatch internshipBatch;

    @Column(name = "duration_month")
    Integer durationMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    RoadmapStatus status = RoadmapStatus.DRAFT;

    @Column(name = "created_by")
    Long createdBy;

    @Column(name = "created_at")
    Instant createdAt;

    @Column(name = "updated_at")
    Instant updatedAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<RoadmapNode> roadmapNodes;
}
