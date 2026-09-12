package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated job list response")
public class JobListResponse {

    @Schema(description = "List of jobs")
    private List<JobResponse> results;

    @Schema(description = "Current page number (0-based)", example = "0")
    private Integer page;

    @Schema(description = "Total number of pages", example = "5")
    private Integer totalPages;

    @Schema(description = "Total number of jobs", example = "50")
    private Long totalCount;

    @Schema(description = "Page size", example = "10")
    private Integer pageSize;

    @Schema(description = "Whether there is a next page", example = "true")
    private Boolean hasNext;

    @Schema(description = "Whether there is a previous page", example = "false")
    private Boolean hasPrevious;
}