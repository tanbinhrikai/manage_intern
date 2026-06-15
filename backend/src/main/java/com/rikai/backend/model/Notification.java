package com.rikai.backend.model;

import com.rikai.backend.model.Enum.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Users receiver;

    String title;

    String content;
    NotificationType type;

    boolean isRead;


    Long referenceId;

    Instant createdAt;

}