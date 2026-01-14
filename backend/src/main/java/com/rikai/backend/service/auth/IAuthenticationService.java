package com.rikai.backend.service.auth;

import com.rikai.backend.dto.request.AuthenticationRequest;
import com.rikai.backend.dto.response.AuthenticationResponse;

public interface IAuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(String refreshToken);

    com.rikai.backend.model.Users getCurrentUser();
}
