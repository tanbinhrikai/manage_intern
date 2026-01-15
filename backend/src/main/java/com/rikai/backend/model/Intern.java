package com.rikai.backend.model;

import com.rikai.backend.model.Enum.InternStatus;
import com.rikai.backend.model.Enum.OfferStatus;
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
@Table(name = "interns")
public class Intern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Column(name = "email", unique = true)
    String email;

    @Column(name = "phone")
    String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false)
    JobPosition position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    InternshipBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", referencedColumnName = "id")
    Users mentor;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date")
    LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    InternStatus status = InternStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_status")
    @Builder.Default
    OfferStatus offerStatus = OfferStatus.NONE;

    @Column(name = "offer_date")
    LocalDate offerDate;

    @Column(name = "offer_notes", columnDefinition = "TEXT")
    String offerNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
