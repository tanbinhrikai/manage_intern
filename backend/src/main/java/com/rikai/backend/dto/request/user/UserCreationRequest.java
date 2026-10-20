package com.rikai.backend.dto.request.user;


import com.rikai.backend.model.Enum.RoleType;
import com.rikai.backend.validation.PasswordConstraint;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Email(message = "INVALID_EMAIL")
    String email;
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(max = 72, message = "PASSWORD_TOO_LONG")
    @PasswordConstraint
    String password;
    @NotBlank(message = "INVALID_FULLNAME")
    String fullName;
    @NotNull(message = "ROLE_REQUIRED")
    RoleType roleName;

    LocalDate dateOfBirth;

    @NotNull(message = "DEPARTMENT_REQUIRED")
    Long departmentId;
}
