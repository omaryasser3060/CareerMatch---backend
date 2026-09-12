package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.JobListResponse;
import com.example.backend.dto.response.JobResponse;
import com.example.backend.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Job Discovery", description = "Job search and discovery endpoints")
public class JobController {

    private final JobService jobService;

    @GetMapping
    @Operation(summary = "Search jobs", description = "Search and filter job postings with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Jobs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public ResponseEntity<ApiResponse<JobListResponse>> searchJobs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) String remoteType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @PageableDefault(size = 10, sort = "postedDate") Pageable pageable
    ) {
        log.debug("Job search: query={}, location={}, page={}", query, location, pageable.getPageNumber());

        JobListResponse response = jobService.searchJobs(
                query, location, employmentType, remoteType,
                minSalary, maxSalary, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job details", description = "Returns detailed information about a specific job")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Job found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<ApiResponse<JobResponse>> getJobDetails(
            @PathVariable String jobId
    ) {
        log.debug("Job details request: {}", jobId);
        JobResponse response = jobService.getJobDetails(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent jobs", description = "Returns the most recently posted jobs")
    public ResponseEntity<ApiResponse<JobListResponse>> getRecentJobs(
            @RequestParam(required = false, defaultValue = "7") @Min(1) @Max(30) Integer days,
            @PageableDefault(size = 10, sort = "postedDate") Pageable pageable
    ) {
        JobListResponse response = jobService.getRecentJobs(days, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/companies")
    @Operation(summary = "Get distinct companies", description = "Returns a list of distinct company names")
    public ResponseEntity<ApiResponse<List<String>>> getCompanies() {
        List<String> companies = jobService.getDistinctCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    @GetMapping("/locations")
    @Operation(summary = "Get distinct locations", description = "Returns a list of distinct job locations")
    public ResponseEntity<ApiResponse<List<String>>> getLocations() {
        List<String> locations = jobService.getDistinctLocations();
        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    @GetMapping("/employment-types")
    @Operation(summary = "Get employment types", description = "Returns a list of distinct employment types")
    public ResponseEntity<ApiResponse<List<String>>> getEmploymentTypes() {
        List<String> types = jobService.getDistinctEmploymentTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }

    @GetMapping("/remote-types")
    @Operation(summary = "Get remote types", description = "Returns a list of distinct remote types")
    public ResponseEntity<ApiResponse<List<String>>> getRemoteTypes() {
        List<String> types = jobService.getDistinctRemoteTypes();
        return ResponseEntity.ok(ApiResponse.success(types));
    }
}