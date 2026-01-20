package com.rikai.backend.dto.response.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.dto.response.department.DepartmentResponse;
import com.rikai.backend.model.Users;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private RolesResponse role;
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
        RolesResponse roleResponse = null;
        if (user.getRole() != null) {
            roleResponse = RolesResponse.builder()
                    .roleName(user.getRole().getRoleName())
                    .description(user.getRole().getDescription())
                    .build();
        }

        return UserResponse.builder()
                .id(String.valueOf(user.getId()))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .department(deptResponse)
                .role(roleResponse)
                .build();
    }}

    