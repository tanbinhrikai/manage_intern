package com.rikai.backend.model;

import com.rikai.backend.model.Enum.CriteriaCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

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
    Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    CriteriaCategory category;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "weight", precision = 5, scale = 2)
    @Builder.Default
    BigDecimal weight = BigDecimal.ONE;

    @OneToMany(mappedBy = "criteria")
    List<CriteriaScoreDefinition> scoreDefinitions;
}