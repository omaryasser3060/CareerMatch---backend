package com.example.backend.repository;

import com.example.backend.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    // ============================================================
    // Find Methods
    // ============================================================

    Optional<Job> findByExternalId(String externalId);

    Optional<Job> findByExternalIdAndSource(String externalId, String source);

    Optional<Job> findByIdAndSource(String id, String source);

    List<Job> findBySource(String source);

    List<Job> findBySourceOrderByPostedDateDesc(String source);

    List<Job> findByCompany(String company);

    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByLocationContainingIgnoreCase(String location);

    @Query(
            value = "SELECT j FROM Job j WHERE " +
                    "(cast(:query as string) IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
                    "LOWER(j.company) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
                    "LOWER(j.description) LIKE LOWER(CONCAT('%', cast(:query as string), '%'))) AND " +
                    "(cast(:location as string) IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', cast(:location as string), '%'))) AND " +
                    "(cast(:employmentType as string) IS NULL OR j.employmentType = :employmentType) AND " +
                    "(cast(:remoteType as string) IS NULL OR j.remoteType = :remoteType) AND " +
                    "(cast(:minSalary as bigdecimal) IS NULL OR j.salaryMin >= :minSalary) AND " +
                    "(cast(:maxSalary as bigdecimal) IS NULL OR j.salaryMax <= :maxSalary) AND " +
                    "(cast(:source as string) IS NULL OR j.source = :source)",
            countQuery = "SELECT COUNT(j) FROM Job j WHERE " +
                    "(cast(:query as string) IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
                    "LOWER(j.company) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
                    "LOWER(j.description) LIKE LOWER(CONCAT('%', cast(:query as string), '%'))) AND " +
                    "(cast(:location as string) IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', cast(:location as string), '%'))) AND " +
                    "(cast(:employmentType as string) IS NULL OR j.employmentType = :employmentType) AND " +
                    "(cast(:remoteType as string) IS NULL OR j.remoteType = :remoteType) AND " +
                    "(cast(:minSalary as bigdecimal) IS NULL OR j.salaryMin >= :minSalary) AND " +
                    "(cast(:maxSalary as bigdecimal) IS NULL OR j.salaryMax <= :maxSalary) AND " +
                    "(cast(:source as string) IS NULL OR j.source = :source)")
    Page<Job> searchJobs(@Param("query") String query,
                         @Param("location") String location,
                         @Param("employmentType") String employmentType,
                         @Param("remoteType") String remoteType,
                         @Param("minSalary") BigDecimal minSalary,
                         @Param("maxSalary") BigDecimal maxSalary,
                         @Param("source") String source,
                         Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.postedDate >= :fromDate ORDER BY j.postedDate DESC")
    Page<Job> findRecentJobs(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    @Query("SELECT DISTINCT j.company FROM Job j ORDER BY j.company")
    List<String> findDistinctCompanies();

    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.location IS NOT NULL ORDER BY j.location")
    List<String> findDistinctLocations();

    @Query("SELECT DISTINCT j.employmentType FROM Job j WHERE j.employmentType IS NOT NULL ORDER BY j.employmentType")
    List<String> findDistinctEmploymentTypes();

    @Query("SELECT DISTINCT j.remoteType FROM Job j WHERE j.remoteType IS NOT NULL ORDER BY j.remoteType")
    List<String> findDistinctRemoteTypes();

    // ============================================================
    // Exists Methods
    // ============================================================

    boolean existsByExternalId(String externalId);

    boolean existsByExternalIdAndSource(String externalId, String source);

    boolean existsByTitleAndCompany(String title, String company);

    // ============================================================
    // Count Methods
    // ============================================================

    long countBySource(String source);

    long countByCompany(String company);

    long countByPostedDateAfter(LocalDateTime date);

    long countByLastSyncAtAfter(LocalDateTime date);

    // ============================================================
    // Update Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.lastSyncAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    int updateLastSyncAt(@Param("id") String id);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.requiredSkillsJson = :requiredSkills, " +
            "j.preferredSkillsJson = :preferredSkills WHERE j.id = :id")
    int updateSkills(@Param("id") String id,
                     @Param("requiredSkills") String requiredSkills,
                     @Param("preferredSkills") String preferredSkills);

    // ============================================================
    // Delete Methods
    // ============================================================

    @Modifying
    @Transactional
    @Query("DELETE FROM Job j WHERE j.source = :source")
    int deleteBySource(@Param("source") String source);

    @Modifying
    @Transactional
    @Query("DELETE FROM Job j WHERE j.postedDate < :date")
    int deleteOlderThan(@Param("date") LocalDateTime date);

    @Modifying
    @Transactional
    @Query("DELETE FROM Job j WHERE j.lastSyncAt < :date AND j.source = 'adzuna'")
    int deleteStaleAdzunaJobs(@Param("date") LocalDateTime date);
}