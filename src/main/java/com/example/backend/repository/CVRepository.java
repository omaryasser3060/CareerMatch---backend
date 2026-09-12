package com.example.backend.repository;

import com.example.backend.model.CV;
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
public interface CVRepository extends JpaRepository<CV, String> {

    @EntityGraph(attributePaths = {"user"})
    Optional<CV> findByIdAndUserId(String id, String userId);

    @EntityGraph(attributePaths = {"user"})
    List<CV> findByUserIdOrderByUploadedAtDesc(String userId);

    @EntityGraph(attributePaths = {"user"})
    Page<CV> findByUserIdOrderByUploadedAtDesc(String userId, Pageable pageable);

    List<CV> findByUserIdAndParsed(String userId, boolean parsed);

    List<CV> findByUserIdAndParsedTrue(String userId);

    List<CV> findByUserIdAndParsedFalse(String userId);

    @Query("SELECT c FROM CV c WHERE c.user.id = :userId AND " +
            "(:parsed IS NULL OR c.parsed = :parsed) AND " +
            "(:fromDate IS NULL OR c.uploadedAt >= :fromDate) AND " +
            "(:toDate IS NULL OR c.uploadedAt <= :toDate) " +
            "ORDER BY c.uploadedAt DESC")
    Page<CV> filterUserCVs(@Param("userId") String userId,
                           @Param("parsed") Boolean parsed,
                           @Param("fromDate") LocalDateTime fromDate,
                           @Param("toDate") LocalDateTime toDate,
                           Pageable pageable);

    @Query("SELECT c FROM CV c WHERE c.user.id = :userId ORDER BY c.uploadedAt DESC")
    List<CV> findLatestByUserId(@Param("userId") String userId, Pageable pageable);

    boolean existsByIdAndUserId(String id, String userId);

    boolean existsByUserIdAndFilename(String userId, String filename);

    boolean existsByUserIdAndParsed(String userId, boolean parsed);

    long countByUserId(String userId);

    long countByUserIdAndParsed(String userId, boolean parsed);

    long countByParsed(boolean parsed);

    long countByUploadedAtAfter(LocalDateTime date);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE CV c SET c.parsed = true, c.parsedAt = CURRENT_TIMESTAMP, " +
            "c.rawText = :rawText, c.skillsJson = :skillsJson, " +
            "c.profileJson = :profileJson, c.extractionConfidence = :confidence " +
            "WHERE c.id = :id")
    int markAsParsed(@Param("id") String id,
                     @Param("rawText") String rawText,
                     @Param("skillsJson") String skillsJson,
                     @Param("profileJson") String profileJson,
                     @Param("confidence") String confidence);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE CV c SET c.rawText = :rawText WHERE c.id = :id")
    int updateRawText(@Param("id") String id, @Param("rawText") String rawText);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE CV c SET c.skillsJson = :skillsJson WHERE c.id = :id")
    int updateSkillsJson(@Param("id") String id, @Param("skillsJson") String skillsJson);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE CV c SET c.profileJson = :profileJson WHERE c.id = :id")
    int updateProfileJson(@Param("id") String id, @Param("profileJson") String profileJson);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE CV c SET c.parsed = :parsed, c.extractionConfidence = :confidence " +
            "WHERE c.id = :id")
    int updateProcessingFailure(@Param("id") String id,
                                @Param("parsed") boolean parsed,
                                @Param("confidence") String confidence);

    @Modifying
    @Transactional
    @Query("DELETE FROM CV c WHERE c.id = :id AND c.user.id = :userId")
    int deleteByIdAndUserId(@Param("id") String id, @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CV c WHERE c.user.id = :userId")
    int deleteByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CV c WHERE c.parsed = false AND c.uploadedAt < :date")
    int deleteUnparsedOlderThan(@Param("date") LocalDateTime date);
}