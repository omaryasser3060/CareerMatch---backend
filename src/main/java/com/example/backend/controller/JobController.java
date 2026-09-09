package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.JobListResponse;
import com.example.backend.dto.response.JobResponse;
import com.example.backend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<JobListResponse>> getJobs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String remoteType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary
    ) {
        JobListResponse response = jobService.searchJobs(
                query, location, page, pageSize,
                employmentType, remoteType, minSalary, maxSalary
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobDetails(@PathVariable String jobId) {
        JobResponse response = jobService.getJobDetails(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}