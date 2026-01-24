package com.rikai.backend.model;

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
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    Users user;

    @Column(name = "action", nullable = false)
    String action;

    @Column(name = "entity_type")
    String entityType;

    @Column(name = "entity_id")
    Integer entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    String newValue;

    @Column(name = "details", columnDefinition = "TEXT")
    String details;

    @Column(name = "ip_address")
    String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
