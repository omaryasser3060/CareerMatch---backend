package com.example.backend.service;

import com.example.backend.dto.response.JobListResponse;
import com.example.backend.dto.response.JobResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Job;
import com.example.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    public JobListResponse searchJobs(
            String query,
            String location,
            Integer page,
            Integer pageSize,
            String employmentType,
            String remoteType,
            BigDecimal minSalary,
            BigDecimal maxSalary
    ) {
        int currentPage = page != null ? page : 1;
        int size = pageSize != null ? pageSize : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, size);

        Double minSalaryDouble = minSalary != null ? minSalary.doubleValue() : null;
        Double maxSalaryDouble = maxSalary != null ? maxSalary.doubleValue() : null;

        Page<Job> jobPage = jobRepository.searchJobs(
                query,
                location,
                employmentType,
                remoteType,
                minSalaryDouble,
                maxSalaryDouble,
                pageable
        );

        List<JobResponse> jobResponses = jobPage.getContent()
                .stream()
                .map(JobResponse::fromEntity)
                .collect(Collectors.toList());

        return JobListResponse.builder()
                .results(jobResponses)
                .page(currentPage)
                .totalPages(jobPage.getTotalPages())
                .totalCount((int) jobPage.getTotalElements())
                .pageSize(size)
                .build();
    }

    public JobResponse getJobDetails(String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        return JobResponse.fromEntity(job);
    }

    public Job getJobEntity(String jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
    }

    @org.springframework.transaction.annotation.Transactional
    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    public List<Job> getJobsBySource(String source) {
        return jobRepository.findBySource(source);
    }
}