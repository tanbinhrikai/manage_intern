package com.rikai.backend.model;

import com.rikai.backend.model.Enum.EvaluationConclusion;
import com.rikai.backend.model.Enum.SessionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "evaluation_sessions")
public class EvaluationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", referencedColumnName = "id", nullable = false)
    Intern intern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", referencedColumnName = "id", nullable = false)
    Users mentor;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    SessionType sessionType;

    @Column(name = "evaluation_date", nullable = false)
    LocalDate evaluationDate;

    @Column(name = "final_score", precision = 5, scale = 2)
    BigDecimal finalScore;

    @Column(name = "level_assessment")
    String levelAssessment;

    @Enumerated(EnumType.STRING)
    @Column(name = "conclusion")
    EvaluationConclusion conclusion;

    @Column(name = "overall_comment", columnDefinition = "TEXT")
    String overallComment;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Set<EvaluationScore> scores;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
