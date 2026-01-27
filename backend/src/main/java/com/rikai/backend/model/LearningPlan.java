package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "learning_plans")
public class LearningPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id")
    Intern intern;

    @Column(name = "title", columnDefinition = "TEXT")
    String title;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "generated_from_prompt", columnDefinition = "TEXT")
    String generatedFromPrompt;

    @OneToMany(mappedBy = "learningPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<PlanModule> modules;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}