package com.rikai.backend.repository;


import com.rikai.backend.model.RefreshToken;
import com.rikai.backend.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken , Long> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
