    package com.rikai.backend.model;

    import com.rikai.backend.model.Enum.AttemptStatus;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "test_attempts")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder

    // bài làm thực tế
    public class TestAttempt {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "assignment_id")
        private TestAssignment assignment;

        private Instant startedAt;

        private Instant submittedAt;

        private Double score;

        @Enumerated(EnumType.STRING)
        private AttemptStatus status;

        @OneToMany(mappedBy = "attempt",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
        private List<AttemptAnswer> answers = new ArrayList<>();
    }