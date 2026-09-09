package com.example.backend.repository;

import com.example.backend.model.MatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, String> {

    Page<MatchResult> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<MatchResult> findByIdAndUserId(String id, String userId);

    List<MatchResult> findByUserId(String userId);

    @Query("SELECT m FROM MatchResult m WHERE m.user.id = :userId AND " +
            "(:minScore IS NULL OR m.overallMatchScore >= :minScore) AND " +
            "(:maxScore IS NULL OR m.overallMatchScore <= :maxScore) AND " +
            "(:jobTitle IS NULL OR LOWER(m.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
            "(:dateFrom IS NULL OR m.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR m.createdAt <= :dateTo)")
    Page<MatchResult> filterMatches(@Param("userId") String userId,
                                    @Param("minScore") Integer minScore,
                                    @Param("maxScore") Integer maxScore,
                                    @Param("jobTitle") String jobTitle,
                                    @Param("dateFrom") LocalDateTime dateFrom,
                                    @Param("dateTo") LocalDateTime dateTo,
                                    Pageable pageable);

    @Query("SELECT AVG(m.overallMatchScore) FROM MatchResult m WHERE m.user.id = :userId")
    Double getAverageMatchScore(@Param("userId") String userId);

    @Query("SELECT COUNT(m) FROM MatchResult m WHERE m.user.id = :userId")
    long countByUserId(@Param("userId") String userId);
}