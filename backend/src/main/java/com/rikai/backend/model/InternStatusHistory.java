package com.rikai.backend.model;

import com.rikai.backend.model.Enum.InternStatus;
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
@Table(name = "intern_status_history")
public class InternStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", referencedColumnName = "id", nullable = false)
    Intern intern;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    InternStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    InternStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", referencedColumnName = "id", nullable = false)
    Users changedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    String reason;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    Instant changedAt;
}
