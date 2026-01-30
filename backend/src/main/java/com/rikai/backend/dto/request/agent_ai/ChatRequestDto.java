package com.rikai.backend.dto.request.agent_ai;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRequestDto {
    String message;
    Long positionId;
    String duration;
    Long batchId;

}
