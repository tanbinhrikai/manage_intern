package com.rikai.backend.dto.request.user;

import com.rikai.backend.validation.PasswordConstraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "OLD_PASSWORD_REQUIRED")
    String oldPassword;

    @NotBlank(message = "NEW_PASSWORD_REQUIRED")
    @Size(max = 72, message = "PASSWORD_TOO_LONG")
    @PasswordConstraint
    String newPassword;

    @NotBlank(message = "CONFIRM_PASSWORD_REQUIRED")
    String confirmPassword;
}
