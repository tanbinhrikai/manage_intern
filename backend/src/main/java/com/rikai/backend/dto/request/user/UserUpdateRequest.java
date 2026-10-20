package com.rikai.backend.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

import com.rikai.backend.validation.PasswordConstraint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Email(message = "INVALID_EMAIL")
    String email;
    @Size(max = 72, message = "PASSWORD_TOO_LONG")
    @PasswordConstraint
    String password;
    @NotBlank(message = "INVALID_FULLNAME")
    String fullName;
    LocalDate dateOfBirth;
    boolean isActive;

    @NotNull(message = "DEPARTMENT_REQUIRED")
    Long departmentId;
}
