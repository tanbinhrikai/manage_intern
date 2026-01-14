package com.rikai.backend.dto.response.user;

import com.rikai.backend.dto.response.DepartmentResponse;
import com.rikai.backend.model.Department;
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
    private String id;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    @Builder.Default
    private boolean isActive = true;
    private Instant createdAt;
    private Instant updatedAt;
    private Roles role;
    private DepartmentResponse department;

    public static UserResponse fromUser(Users user) {
        if (user == null) {
            return null;
        }
        DepartmentResponse deptResponse = null;
        if (user.getDepartment() != null) {
            deptResponse = DepartmentResponse.builder()
                    .id(user.getDepartment().getId())
                    .title(user.getDepartment().getTitle())
                    .build();
        }
        return UserResponse.builder()
                .id(String.valueOf(user.getId()))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .department(deptResponse)
                .role(user.getRole())
                .build();
    }
}
