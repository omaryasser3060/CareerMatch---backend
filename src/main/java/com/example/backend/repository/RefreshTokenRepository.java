package com.example.backend.repository;

import com.example.backend.model.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    // ============================================================
    // Find Methods
    // ============================================================

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByToken(String token);

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @EntityGraph(attributePaths = {"user"})
    Optional<RefreshToken> findByUserId(String userId);

    @EntityGraph(attributePaths = {"user"})
    List<RefreshToken> findAllByUserId(String userId);

    @EntityGraph(attributePaths = {"user"})
    List<RefreshToken> findByUserIdAndRevokedFalse(String userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now AND rt.revoked = false")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :date")
    List<RefreshToken> findRevokedBefore(@Param("date") LocalDateTime date);

    // ============================================================
    // Exists Methods
    // ============================================================

    boolean existsByToken(String token);

    boolean existsByUserId(String userId);

    boolean existsByUserIdAndRevokedFalse(String userId);

    // ============================================================
    // Count Methods
    // ============================================================

    long countByUserId(String userId);

    long countByUserIdAndRevokedFalse(String userId);

    long countByRevoked(boolean revoked);

    long countByExpiresAtBefore(LocalDateTime date);

    // ============================================================
    // Update Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.user.id = :userId")
    int revokeAllByUserId(@Param("userId") String userId);

    // ============================================================
    // Delete Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    int deleteByToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :date OR rt.revoked = true")
    int deleteExpiredAndRevoked(@Param("date") LocalDateTime date);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :date")
    int deleteRevokedBefore(@Param("date") LocalDateTime date);
}