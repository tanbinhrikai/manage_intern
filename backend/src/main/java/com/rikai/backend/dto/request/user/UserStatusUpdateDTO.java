package com.rikai.backend.dto.request.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserStatusUpdateDTO {
    UUID id;
    boolean isActive;
}
