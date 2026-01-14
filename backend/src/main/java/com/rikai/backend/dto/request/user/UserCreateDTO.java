package com.rikai.backend.dto.request.user;

import com.rikai.backend.validation.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCreateDTO {
    @Email(message = "INVALID_EMAIL")
    private String email;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "PASSWORD_WEAK"
    )
    private String password;
    private String fullName;
    @DobConstraint(min = 18)
    private LocalDate dateOfBirth;
}
