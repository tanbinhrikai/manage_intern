package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

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

    @Column(name = "output_quality", columnDefinition = "TEXT")
    String outputQuality;

    @Column(name = "proactivity_score")
    Byte proactivityScore;

    @Column(name = "progress_score")
    Byte progressScore;

    @Column(name = "issues_risks", columnDefinition = "TEXT")
    String issuesRisks;

    @Column(name = "mentor_overall_comment", columnDefinition = "TEXT")
    String mentorOverallComment;

    @Column(name = "status")
    @Builder.Default
    String status = "submitted";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
