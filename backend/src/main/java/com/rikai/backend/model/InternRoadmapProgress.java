package com.rikai.backend.model;

import com.rikai.backend.model.Enum.RoadmapStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "intern_roadmap_progress")
public class InternRoadmapProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", referencedColumnName = "id", nullable = false)
    Intern intern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", referencedColumnName = "id", nullable = false)
    InternshipRoadmap roadmap;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    RoadmapStatus status = RoadmapStatus.PENDING;

    @Column(name = "start_date")
    LocalDate startDate;

    @Column(name = "expected_end_date")
    LocalDate expectedEndDate;

    @Column(name = "actual_completion_date")
    LocalDate actualCompletionDate;

    @Column(name = "mentor_notes", columnDefinition = "TEXT")
    String mentorNotes;
}
