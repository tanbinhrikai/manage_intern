package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "weekly_reports")
public class WeeklyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", referencedColumnName = "id", nullable = false)
    Intern intern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", referencedColumnName = "id", nullable = false)
    Users mentor;

    @Column(name = "week_number")
    Integer weekNumber;

    @Column(name = "week_start_date", nullable = false)
    LocalDate weekStartDate;

    @Column(name = "tasks_assigned", columnDefinition = "TEXT")
    String tasksAssigned;

    @Column(name = "tasks_completed", columnDefinition = "TEXT")
    String tasksCompleted;

    @Column(name = "average_score", precision = 4, scale = 2)
    @Builder.Default
    BigDecimal averageScore = BigDecimal.ZERO;

    @Column(name = "issues_risks", columnDefinition = "TEXT")
    String issuesRisks;

    @Column(name = "mentor_overall_comment", columnDefinition = "TEXT")
    String mentorOverallComment;

    @Column(name = "status")
    @Builder.Default
    String status = "submitted";

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    List<WeeklyReportDetail> details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void updateAverageScore() {
        if (this.details == null || this.details.isEmpty()) {
            this.averageScore = BigDecimal.ZERO;
            return;
        }

        double average = this.details.stream()
                .filter(detail -> detail.getScore() != null)
                .mapToInt(detail -> detail.getScore().intValue())
                .average()
                .orElse(0.0);

        this.averageScore = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }
}