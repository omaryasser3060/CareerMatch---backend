package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated match history response")
public class MatchHistoryResponse {

    @Schema(description = "List of match summaries")
    private List<MatchSummaryResponse> results;

    @Schema(description = "Current page number (0-based)", example = "0")
    private Integer page;

    @Schema(description = "Total number of pages", example = "3")
    private Integer totalPages;

    @Schema(description = "Total number of matches", example = "25")
    private Long totalCount;

    @Schema(description = "Page size", example = "10")
    private Integer pageSize;

    @Schema(description = "Whether there is a next page", example = "true")
    private Boolean hasNext;

    @Schema(description = "Whether there is a previous page", example = "false")
    private Boolean hasPrevious;

    // ============================================================
    // Nested DTO
    // ============================================================

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Match summary")
    public static class MatchSummaryResponse {

        @Schema(description = "Match result ID")
        private String matchId;

        @Schema(description = "Overall match score (0-100)", example = "82")
        private Integer overallMatchScore;

        @Schema(description = "Job title")
        private String jobTitle;

        @Schema(description = "Company name")
        private String companyName;

        @Schema(description = "Required skills match count", example = "3")
        private Integer requiredSkillsMatch;

        @Schema(description = "Total required skills count", example = "4")
        private Integer requiredSkillsTotal;

        @Schema(description = "Match timestamp")
        private LocalDateTime createdAt;
    }
}