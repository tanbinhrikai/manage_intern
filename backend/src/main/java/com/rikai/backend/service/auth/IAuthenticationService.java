package com.rikai.backend.service.auth;

import com.rikai.backend.dto.request.authentication.AuthenticationRequest;
import com.rikai.backend.dto.response.authentication.AuthenticationResponse;
import com.rikai.backend.model.Users;

public interface IAuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(String refreshToken);

    Users getCurrentUser();
}
