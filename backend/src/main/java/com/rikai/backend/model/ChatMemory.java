package com.rikai.backend.model;


import com.rikai.backend.model.Enum.ChatMemoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "SPRING_AI_CHAT_MEMORY")
public class ChatMemory {
    @Id
    @Column(name = "conversation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    UUID conversationId;

    @Column(name = "content")
    String content;

    @Column(name = "type")
    ChatMemoryType type;

    @Column(name = "timestamp")
    Instant timestamp;
}
