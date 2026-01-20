package com.rikai.backend.dto.response.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rikai.backend.dto.response.user.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    String accessToken;
    String refreshToken;
    UserResponse userResponse;
}
