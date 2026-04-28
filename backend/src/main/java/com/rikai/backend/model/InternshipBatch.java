package com.rikai.backend.model;

import com.rikai.backend.model.Enum.BatchStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "internship_batches")
public class InternshipBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    BatchStatus status = BatchStatus.ONGOING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @OneToMany(mappedBy = "internshipBatch", cascade = CascadeType.ALL)
    List<Intern> interns;
}
