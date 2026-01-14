package com.rikai.backend.dto.response.user;

import com.rikai.backend.model.Roles;
import com.rikai.backend.model.Users;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private boolean isActive = true;
    private Instant createdAt;
    private Instant updatedAt;
    private Roles role;

    public static UserResponse fromUser(Users user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .role(user.getRole())
                .build();
    }
}
