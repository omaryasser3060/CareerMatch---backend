package com.example.backend.service;

import com.example.backend.dto.response.JobListResponse;
import com.example.backend.dto.response.JobResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Job;
import com.example.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;

    // ============================================================
    // Search
    // ============================================================

    @Transactional(readOnly = true)
    public JobListResponse searchJobs(
            String query,
            String location,
            String employmentType,
            String remoteType,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Pageable pageable
    ) {
        log.debug("Searching jobs: query={}, location={}, page={}", query, location, pageable.getPageNumber());

        Page<Job> jobPage = jobRepository.searchJobs(
                query, location, employmentType, remoteType,
                minSalary, maxSalary, null, pageable
        );

        return buildJobListResponse(jobPage);
    }

    @Transactional(readOnly = true)
    public JobListResponse getRecentJobs(Integer days, Pageable pageable) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        Page<Job> jobPage = jobRepository.findRecentJobs(fromDate, pageable);
        return buildJobListResponse(jobPage);
    }

    // ============================================================
    // Get Job
    // ============================================================

    @Transactional(readOnly = true)
    public JobResponse getJobDetails(String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        return JobResponse.fromEntity(job);
    }

    @Transactional(readOnly = true)
    public Job getJobEntity(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
    }

    // ============================================================
    // Get Distinct Values
    // ============================================================

    @Transactional(readOnly = true)
    public List<String> getDistinctCompanies() {
        return jobRepository.findDistinctCompanies();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctLocations() {
        return jobRepository.findDistinctLocations();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctEmploymentTypes() {
        return jobRepository.findDistinctEmploymentTypes();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctRemoteTypes() {
        return jobRepository.findDistinctRemoteTypes();
    }

    // ============================================================
    // Save (for Adzuna sync)
    // ============================================================

    @Transactional
    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    @Transactional
    public List<Job> saveAllJobs(List<Job> jobs) {
        return jobRepository.saveAll(jobs);
    }

    @Transactional(readOnly = true)
    public boolean existsByExternalId(String externalId) {
        return jobRepository.existsByExternalId(externalId);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private JobListResponse buildJobListResponse(Page<Job> jobPage) {
        List<JobResponse> jobResponses = jobPage.getContent()
                .stream()
                .map(JobResponse::fromEntity)
                .toList();

        return JobListResponse.builder()
                .results(jobResponses)
                .page(jobPage.getNumber())
                .totalPages(jobPage.getTotalPages())
                .totalCount(jobPage.getTotalElements())
                .pageSize(jobPage.getSize())
                .hasNext(jobPage.hasNext())
                .hasPrevious(jobPage.hasPrevious())
                .build();
    }
}