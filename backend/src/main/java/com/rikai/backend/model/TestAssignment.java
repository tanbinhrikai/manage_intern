package com.rikai.backend.model;

import com.rikai.backend.model.Enum.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
// bài kiểm tra này cho ai
@Entity
@Table(name = "test_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id")
    private Intern intern;

    private Integer evaluationNumber;

    private Instant assignedAt;

    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;
}
