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
@Schema(description = "Improvement plan response")
public class ImprovementPlanResponse {

    @Schema(description = "List of recommendations")
    private List<RecommendationResponse> recommendations;

    @Schema(description = "Plan summary")
    private Summary summary;

    @Schema(description = "Plan creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Plan last update timestamp")
    private LocalDateTime updatedAt;

    // ============================================================
    // Nested DTOs
    // ============================================================

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Improvement recommendation")
    public static class RecommendationResponse {

        @Schema(description = "Gap ID")
        private String gapId;

        @Schema(description = "Gap name", example = "Docker")
        private String gapName;

        @Schema(description = "Gap category", example = "DEVOPS")
        private String gapCategory;

        @Schema(description = "Required or preferred", example = "REQUIRED")
        private String requiredOrPreferred;

        @Schema(description = "Importance weight (1-10)", example = "8")
        private Integer importanceWeight;

        @Schema(description = "Evidence from job description")
        private String jobEvidence;

        @Schema(description = "Evidence from CV")
        private String cvEvidence;

        @Schema(description = "Related existing strengths")
        private List<String> relatedExistingStrengths;

        @Schema(description = "Recommended action")
        private String recommendedAction;

        @Schema(description = "Expected deliverable")
        private String deliverable;

        @Schema(description = "Estimated effort", example = "1-2 weeks")
        private String estimatedEffort;

        @Schema(description = "Expected score gain (0-100)", example = "15")
        private Integer expectedScoreGain;

        @Schema(description = "Priority score (0-100)", example = "85")
        private Integer priorityScore;

        @Schema(description = "Priority label", example = "HIGH")
        private String priorityLabel;

        @Schema(description = "Confidence", example = "HIGH")
        private String confidence;

        @Schema(description = "Learning resources")
        private List<ResourceResponse> resources;

        @Schema(description = "Status", example = "PENDING")
        private String status;

        @Schema(description = "Completion timestamp")
        private LocalDateTime completedAt;

        @Schema(description = "Creation timestamp")
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Learning resource")
    public static class ResourceResponse {

        @Schema(description = "Resource title")
        private String title;

        @Schema(description = "Resource URL")
        private String url;

        @Schema(description = "Resource type", example = "COURSE")
        private String type;

        @Schema(description = "Resource description")
        private String description;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Plan summary")
    public static class Summary {

        @Schema(description = "Total number of gaps", example = "5")
        private Integer totalGaps;

        @Schema(description = "Number of high priority gaps", example = "2")
        private Integer highPriority;

        @Schema(description = "Number of medium priority gaps", example = "2")
        private Integer mediumPriority;

        @Schema(description = "Number of low priority gaps", example = "1")
        private Integer lowPriority;

        @Schema(description = "Total expected score gain", example = "35")
        private Integer expectedScoreGainTotal;
    }
}