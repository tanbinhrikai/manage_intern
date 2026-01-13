package com.rikai.backend.dto.request;


import com.rikai.backend.validation.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "PASSWORD_WEAK"
    )
    String password;
    @NotBlank(message = "INVALID_FULLNAME")
    String fullName;

    @DobConstraint(min = 10)
    LocalDate dateOfBirth;
}
