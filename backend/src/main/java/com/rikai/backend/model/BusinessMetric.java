package com.rikai.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "business_metrics")
public class BusinessMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", referencedColumnName = "id", nullable = false)
    Intern intern;

    @Column(name = "metric_name", nullable = false)
    String metricName;

    @Column(name = "metric_category")
    String metricCategory;

    @Column(name = "target_value", precision = 10, scale = 2)
    BigDecimal targetValue;

    @Column(name = "actual_value", precision = 10, scale = 2)
    BigDecimal actualValue;

    @Column(name = "unit")
    String unit;

    @Column(name = "recorded_date", nullable = false)
    LocalDate recordedDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    Users createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
