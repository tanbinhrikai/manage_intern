package com.rikai.backend.service.token;

import com.rikai.backend.dto.response.AuthenticationResponse;
import com.rikai.backend.model.Users;

public interface ITokenService {
    void saveRefreshToken(Users user, String refreshToken);
     AuthenticationResponse refreshToken(String refreshToken);
     String generateAccessToken(Users user);
     String hashToken(String token);
}
