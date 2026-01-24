package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "evaluation_criteria")
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

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<EvaluationCriteria> children = new HashSet<>();

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    Integer displayOrder = 0;


    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @OneToMany(mappedBy = "criteria", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<CriteriaScoreDefinition> scoreDefinitions = new HashSet<>();

    public boolean isMainCriteria() {
        return parent == null;
    }

    public boolean isSubCriteria() {
        return parent != null;
    }
}