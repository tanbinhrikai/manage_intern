package com.rikai.backend.model;

import com.rikai.backend.model.Enum.ScoreLabel;
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
@Table(name = "criteria_score_definitions")
public class CriteriaScoreDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", referencedColumnName = "id", nullable = false)
    EvaluationCriteria criteria;

    @Column(name = "score_label", length = 50)
    @Enumerated(EnumType.STRING)
    ScoreLabel scoreLabel;

    @Column(name = "min_score", nullable = false)
    Byte minScore;

    @Column(name = "max_score", nullable = false)
    Byte maxScore;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}