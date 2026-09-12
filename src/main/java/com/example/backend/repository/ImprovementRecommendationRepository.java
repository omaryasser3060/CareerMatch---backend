package com.example.backend.repository;

import com.example.backend.model.ImprovementRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImprovementRecommendationRepository extends JpaRepository<ImprovementRecommendation, String> {

    // ============================================================
    // Find Methods
    // ============================================================

    @EntityGraph(attributePaths = {"matchResult"})
    List<ImprovementRecommendation> findByMatchResultIdOrderByPriorityScoreDesc(String matchResultId);

    @EntityGraph(attributePaths = {"matchResult"})
    Page<ImprovementRecommendation> findByMatchResultIdOrderByPriorityScoreDesc(String matchResultId, Pageable pageable);

    @EntityGraph(attributePaths = {"matchResult"})
    List<ImprovementRecommendation> findByMatchResultIdAndStatus(
            String matchResultId,
            ImprovementRecommendation.RecommendationStatus status
    );

    @EntityGraph(attributePaths = {"matchResult"})
    List<ImprovementRecommendation> findByMatchResultIdAndPriorityLabel(
            String matchResultId,
            ImprovementRecommendation.PriorityLabel priorityLabel
    );

    /**
     * Find by matchResultId and requiredOrPreferred.
     *
     * NOTE: We use @Query instead of method name derivation because
     * "RequiredOrPreferred" contains "Or" which Spring Data interprets
     * as an OR operator, causing query derivation to fail.
     */
    @Query("SELECT ir FROM ImprovementRecommendation ir " +
            "WHERE ir.matchResult.id = :matchResultId " +
            "AND ir.requiredOrPreferred = :requiredOrPreferred " +
            "ORDER BY ir.priorityScore DESC")
    @EntityGraph(attributePaths = {"matchResult"})
    List<ImprovementRecommendation> findByMatchResultIdAndRequiredOrPreferred(
            @Param("matchResultId") String matchResultId,
            @Param("requiredOrPreferred") ImprovementRecommendation.RequiredOrPreferred requiredOrPreferred
    );

    @Query("SELECT ir FROM ImprovementRecommendation ir WHERE ir.matchResult.id = :matchResultId " +
            "AND (:status IS NULL OR ir.status = :status) " +
            "AND (:priorityLabel IS NULL OR ir.priorityLabel = :priorityLabel) " +
            "AND (:requiredOrPreferred IS NULL OR ir.requiredOrPreferred = :requiredOrPreferred) " +
            "ORDER BY ir.priorityScore DESC")
    Page<ImprovementRecommendation> filterRecommendations(
            @Param("matchResultId") String matchResultId,
            @Param("status") ImprovementRecommendation.RecommendationStatus status,
            @Param("priorityLabel") ImprovementRecommendation.PriorityLabel priorityLabel,
            @Param("requiredOrPreferred") ImprovementRecommendation.RequiredOrPreferred requiredOrPreferred,
            Pageable pageable
    );

    @Query("SELECT ir FROM ImprovementRecommendation ir WHERE ir.matchResult.user.id = :userId " +
            "AND ir.status = :status ORDER BY ir.priorityScore DESC")
    List<ImprovementRecommendation> findByUserIdAndStatus(
            @Param("userId") String userId,
            @Param("status") ImprovementRecommendation.RecommendationStatus status
    );

    @Query("SELECT ir FROM ImprovementRecommendation ir WHERE ir.matchResult.user.id = :userId " +
            "AND ir.status = 'PENDING' ORDER BY ir.priorityScore DESC")
    List<ImprovementRecommendation> findPendingByUserId(@Param("userId") String userId);

    @Query("SELECT ir FROM ImprovementRecommendation ir WHERE ir.matchResult.user.id = :userId " +
            "AND ir.status = 'COMPLETED' ORDER BY ir.completedAt DESC")
    List<ImprovementRecommendation> findCompletedByUserId(@Param("userId") String userId);

    // ============================================================
    // Exists Methods
    // ============================================================

    boolean existsByMatchResultIdAndGapName(String matchResultId, String gapName);

    boolean existsByMatchResultId(String matchResultId);

    // ============================================================
    // Count Methods
    // ============================================================

    long countByMatchResultId(String matchResultId);

    long countByMatchResultIdAndStatus(
            String matchResultId,
            ImprovementRecommendation.RecommendationStatus status
    );

    long countByStatus(ImprovementRecommendation.RecommendationStatus status);

    @Query("SELECT COUNT(ir) FROM ImprovementRecommendation ir " +
            "WHERE ir.matchResult.user.id = :userId AND ir.status = :status")
    long countByMatchResultUserIdAndStatus(
            @Param("userId") String userId,
            @Param("status") ImprovementRecommendation.RecommendationStatus status
    );

    long countByCreatedAtAfter(LocalDateTime date);

    // ============================================================
    // Statistics
    // ============================================================

    @Query("SELECT ir.priorityLabel, COUNT(ir) FROM ImprovementRecommendation ir " +
            "WHERE ir.matchResult.id = :matchResultId GROUP BY ir.priorityLabel")
    List<Object[]> countByPriorityLabelGrouped(@Param("matchResultId") String matchResultId);

    @Query("SELECT ir.status, COUNT(ir) FROM ImprovementRecommendation ir " +
            "WHERE ir.matchResult.user.id = :userId GROUP BY ir.status")
    List<Object[]> countByStatusGrouped(@Param("userId") String userId);

    @Query("SELECT AVG(ir.expectedScoreGain) FROM ImprovementRecommendation ir " +
            "WHERE ir.matchResult.id = :matchResultId")
    Double getAverageExpectedScoreGain(@Param("matchResultId") String matchResultId);

    // ============================================================
    // Update Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("UPDATE ImprovementRecommendation ir SET ir.status = :status, " +
            "ir.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE ir.id = :id")
    int updateStatus(
            @Param("id") String id,
            @Param("status") ImprovementRecommendation.RecommendationStatus status
    );

    @Modifying
    @Transactional
    @Query("UPDATE ImprovementRecommendation ir SET ir.status = 'COMPLETED', " +
            "ir.completedAt = CURRENT_TIMESTAMP, ir.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE ir.id = :id")
    int markAsCompleted(@Param("id") String id);

    @Modifying
    @Transactional
    @Query("UPDATE ImprovementRecommendation ir SET ir.status = 'SKIPPED', " +
            "ir.updatedAt = CURRENT_TIMESTAMP WHERE ir.id = :id")
    int markAsSkipped(@Param("id") String id);

    @Modifying
    @Transactional
    @Query("UPDATE ImprovementRecommendation ir SET ir.status = 'IN_PROGRESS', " +
            "ir.updatedAt = CURRENT_TIMESTAMP WHERE ir.id = :id")
    int markAsInProgress(@Param("id") String id);

    // ============================================================
    // Delete Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("DELETE FROM ImprovementRecommendation ir WHERE ir.matchResult.id = :matchResultId")
    int deleteByMatchResultId(@Param("matchResultId") String matchResultId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ImprovementRecommendation ir WHERE ir.status = 'SKIPPED' " +
            "AND ir.updatedAt < :date")
    int deleteSkippedOlderThan(@Param("date") LocalDateTime date);
}