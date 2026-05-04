package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "evaluation_criteria")
@EqualsAndHashCode(exclude = {"children", "scoreDefinitions", "parent", "group"})
@ToString(exclude = {"children", "scoreDefinitions", "parent", "group"})
@SQLDelete(sql = "UPDATE evaluation_criteria SET is_active = false, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class EvaluationCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    CriteriaGroup group;

    @Column(name = "name", nullable = false, length = 255)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "weight", precision = 5, scale = 2)
    @Builder.Default
    BigDecimal weight = BigDecimal.ONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    EvaluationCriteria parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    Set<EvaluationCriteria> children = new HashSet<>();

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @OneToMany(mappedBy = "criteria", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    Set<CriteriaScoreDefinition> scoreDefinitions = new HashSet<>();

    public boolean isMainCriteria() {
        return parent == null;
    }

    public boolean isSubCriteria() {
        return parent != null;
    }

    public void softDelete() {
        this.isActive = false;
        this.deletedAt = Instant.now();
    }
}