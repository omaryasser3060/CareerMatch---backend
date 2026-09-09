package com.example.backend.repository;

import com.example.backend.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserId(String userId);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    void deleteByUserId(String userId);

    boolean existsByUserId(String userId);
}