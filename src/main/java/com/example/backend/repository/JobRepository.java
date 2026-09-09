package com.example.backend.repository;

import com.example.backend.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    @Query("SELECT j FROM Job j WHERE " +
            "(:query IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(j.company) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:employmentType IS NULL OR j.employmentType = :employmentType) AND " +
            "(:remoteType IS NULL OR j.remoteType = :remoteType) AND " +
            "(:minSalary IS NULL OR j.salaryMin >= :minSalary) AND " +
            "(:maxSalary IS NULL OR j.salaryMax <= :maxSalary)")
    Page<Job> searchJobs(@Param("query") String query,
                         @Param("location") String location,
                         @Param("employmentType") String employmentType,
                         @Param("remoteType") String remoteType,
                         @Param("minSalary") Double minSalary,
                         @Param("maxSalary") Double maxSalary,
                         Pageable pageable);

    Optional<Job> findByIdAndSource(String id, String source);

    List<Job> findBySource(String source);
}