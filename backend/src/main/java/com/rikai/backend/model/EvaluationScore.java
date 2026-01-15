package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "evaluation_scores")
public class EvaluationScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "id", nullable = false)
    EvaluationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", referencedColumnName = "id", nullable = false)
    EvaluationCriteria criteria;

    @Column(name = "score")
    Byte score;

    @Column(name = "comment", columnDefinition = "TEXT")
    String comment;
}
