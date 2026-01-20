package com.rikai.backend.dto.request.user;


import com.rikai.backend.validation.DobConstraint;
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
    @Size(min = 8 , message = "INVALID_PASSWORD")
    String password;
    @NotBlank(message = "INVALID_FULLNAME")
    String fullName;

    @DobConstraint(min = 10)
    LocalDate dateOfBirth;

    @NotNull(message = "DEPARTMENT_REQUIRED")
    Long departmentId;
}
