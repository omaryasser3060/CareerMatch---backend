package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface UserRepository extends JpaRepository<User, String> {

    // ============================================================
    // Find Methods
    // ============================================================

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndEmailVerified(String email, boolean emailVerified);

    Optional<User> findByIdAndEmailVerified(String id, boolean emailVerified);

    Page<User> findByRole(User.Role role, Pageable pageable);

    Page<User> findByOnboardingCompleted(boolean onboardingCompleted, Pageable pageable);

    List<User> findByCreatedAtAfter(LocalDateTime date);

    // ============================================================
    // Exists Methods
    // ============================================================

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    // ============================================================
    // Count Methods
    // ============================================================

    long countByRole(User.Role role);

    long countByEmailVerified(boolean emailVerified);

    long countByOnboardingCompleted(boolean onboardingCompleted);

    long countByCreatedAtAfter(LocalDateTime date);

    // ============================================================
    // Update Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = :verified, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateEmailVerified(@Param("id") String id, @Param("verified") boolean verified);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.onboardingCompleted = :completed, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updateOnboardingCompleted(@Param("id") String id, @Param("completed") boolean completed);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :passwordHash, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updatePasswordHash(@Param("id") String id, @Param("passwordHash") String passwordHash);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.preferences = :preferences, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int updatePreferences(@Param("id") String id, @Param("preferences") String preferences);

    // ============================================================
    // Delete Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.id = :id")
    void deleteById(@Param("id") String id);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.email = :email")
    void deleteByEmail(@Param("email") String email);

    // ============================================================
    // Statistics
    // ============================================================

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countByRoleGrouped();

    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u " +
            "WHERE u.createdAt >= :fromDate GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> countByDaySince(@Param("fromDate") LocalDateTime fromDate);
}