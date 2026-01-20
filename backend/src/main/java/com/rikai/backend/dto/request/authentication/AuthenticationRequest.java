package com.rikai.backend.dto.request.authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @Email(message = "INVALID_EMAIL")
    String email;
    @Size(min = 8 , message = "INVALID_PASSWORD")
    String password;
}
