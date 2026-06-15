package com.rikai.backend.dto.response.notification;


import com.rikai.backend.model.Enum.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private boolean isRead;
    private Long referenceId;
    private String link;
    private Instant createdAt;
}