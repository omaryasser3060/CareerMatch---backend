package com.example.backend.repository;

import com.example.backend.model.MatchResult;
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
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, String> {

    // ============================================================
    // Find Methods
    // ============================================================

    @EntityGraph(attributePaths = {"user", "cv", "job"})
    Optional<MatchResult> findByIdAndUserId(String id, String userId);

    @EntityGraph(attributePaths = {"cv", "job"})
    Page<MatchResult> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @EntityGraph(attributePaths = {"cv", "job"})
    List<MatchResult> findByUserId(String userId);

    @EntityGraph(attributePaths = {"cv", "job"})
    List<MatchResult> findByUserIdOrderByCreatedAtDesc(String userId);

    @EntityGraph(attributePaths = {"cv", "job"})
    List<MatchResult> findByCvId(String cvId);

    @EntityGraph(attributePaths = {"cv", "job"})
    List<MatchResult> findByJobId(String jobId);

    @EntityGraph(attributePaths = {"cv", "job"})
    Optional<MatchResult> findByCvIdAndJobId(String cvId, String jobId);

    @Query(
            value = "SELECT m FROM MatchResult m WHERE m.user.id = :userId AND " +
            "(:minScore IS NULL OR m.overallMatchScore >= :minScore) AND " +
            "(:maxScore IS NULL OR m.overallMatchScore <= :maxScore) AND " +
            "(:jobTitle IS NULL OR LOWER(m.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
            "(:companyName IS NULL OR LOWER(m.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
            "(:dateFrom IS NULL OR m.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR m.createdAt <= :dateTo) " +
            "ORDER BY m.createdAt DESC",
            countQuery = "SELECT COUNT(m) FROM MatchResult m WHERE m.user.id = :userId AND " +
                    "(:minScore IS NULL OR m.overallMatchScore >= :minScore) AND " +
                    "(:maxScore IS NULL OR m.overallMatchScore <= :maxScore) AND " +
                    "(:jobTitle IS NULL OR LOWER(m.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
                    "(:companyName IS NULL OR LOWER(m.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) AND " +
                    "(:dateFrom IS NULL OR m.createdAt >= :dateFrom) AND " +
                    "(:dateTo IS NULL OR m.createdAt <= :dateTo)")
    Page<MatchResult> filterMatches(@Param("userId") String userId,
                                    @Param("minScore") Integer minScore,
                                    @Param("maxScore") Integer maxScore,
                                    @Param("jobTitle") String jobTitle,
                                    @Param("companyName") String companyName,
                                    @Param("dateFrom") LocalDateTime dateFrom,
                                    @Param("dateTo") LocalDateTime dateTo,
                                    Pageable pageable);

    @Query("SELECT m FROM MatchResult m WHERE m.user.id = :userId " +
            "ORDER BY m.overallMatchScore DESC")
    List<MatchResult> findTopMatchesByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT m FROM MatchResult m WHERE m.extractionConfidence = 'LOW' " +
            "OR m.humanReviewFlag = true")
    List<MatchResult> findFlaggedForReview();

    // ============================================================
    // Exists Methods
    // ============================================================

    boolean existsByCvIdAndJobId(String cvId, String jobId);

    boolean existsByIdAndUserId(String id, String userId);

    // ============================================================
    // Count Methods
    // ============================================================

    long countByUserId(String userId);

    long countByCvId(String cvId);

    long countByJobId(String jobId);

    long countByHumanReviewFlag(boolean humanReviewFlag);

    long countByCreatedAtAfter(LocalDateTime date);

    // ============================================================
    // Statistics
    // ============================================================

    @Query("SELECT AVG(m.overallMatchScore) FROM MatchResult m WHERE m.user.id = :userId")
    Double getAverageMatchScore(@Param("userId") String userId);

    @Query("SELECT MAX(m.overallMatchScore) FROM MatchResult m WHERE m.user.id = :userId")
    Integer getMaxMatchScore(@Param("userId") String userId);

    @Query("SELECT MIN(m.overallMatchScore) FROM MatchResult m WHERE m.user.id = :userId")
    Integer getMinMatchScore(@Param("userId") String userId);

    @Query("SELECT m.overallMatchScore, COUNT(m) FROM MatchResult m " +
            "WHERE m.user.id = :userId GROUP BY m.overallMatchScore ORDER BY m.overallMatchScore")
    List<Object[]> getScoreDistribution(@Param("userId") String userId);

    @Query("SELECT DATE(m.createdAt), COUNT(m) FROM MatchResult m " +
            "WHERE m.user.id = :userId AND m.createdAt >= :fromDate " +
            "GROUP BY DATE(m.createdAt) ORDER BY DATE(m.createdAt)")
    List<Object[]> countByDaySince(@Param("userId") String userId, @Param("fromDate") LocalDateTime fromDate);

    // ============================================================
    // Update Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("UPDATE MatchResult m SET m.humanReviewFlag = :flag WHERE m.id = :id")
    int updateHumanReviewFlag(@Param("id") String id, @Param("flag") boolean flag);

    // ============================================================
    // Delete Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("DELETE FROM MatchResult m WHERE m.user.id = :userId")
    int deleteByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MatchResult m WHERE m.cv.id = :cvId")
    int deleteByCvId(@Param("cvId") String cvId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MatchResult m WHERE m.job.id = :jobId")
    int deleteByJobId(@Param("jobId") String jobId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MatchResult m WHERE m.createdAt < :date")
    int deleteOlderThan(@Param("date") LocalDateTime date);
}