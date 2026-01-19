package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "evaluation_templates")
public class EvaluationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false)
    Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", referencedColumnName = "id", nullable = false)
    EvaluationCriteria criteria;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    Boolean isRequired = true;

    @Column(name = "weight_override", precision = 5, scale = 2)
    BigDecimal weightOverride;
}
