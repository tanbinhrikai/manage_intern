package com.rikai.backend.model;

import com.rikai.backend.model.Enum.AlertType;
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
@Table(name = "system_alerts")
public class SystemAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    AlertType alertType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", referencedColumnName = "id")
    Users targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", referencedColumnName = "id")
    Intern intern;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    String message;

    @Column(name = "severity")
    @Builder.Default
    String severity = "info";

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    Boolean isRead = false;

    @Column(name = "read_at")
    Instant readAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
