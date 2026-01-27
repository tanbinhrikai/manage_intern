package com.rikai.backend.model;

import com.rikai.backend.model.Enum.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "plan_tasks")
public class PlanTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    PlanModule module;

    @Column(name = "title", columnDefinition = "TEXT")
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "resource_link", columnDefinition = "TEXT")
    String resourceLink;

    @Column(name = "estimated_minutes")
    Integer estimatedMinutes;

    @Enumerated(EnumType.STRING)
    TaskStatus status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_plan_id")
    LearningPlan subPlan;

    Integer orderIndex;
}