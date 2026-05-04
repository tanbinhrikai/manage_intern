package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "criteria_groups")
@SQLDelete(sql = "UPDATE criteria_groups SET is_active = false, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class CriteriaGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", columnDefinition = "TEXT", nullable = false)
    String name;

    @Column(name = "display_order", nullable = false)
    Integer displayOrder;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    List<EvaluationCriteria> criteriaList = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    public void softDelete() {
        this.isActive = false;
        this.deletedAt = Instant.now();
    }
}