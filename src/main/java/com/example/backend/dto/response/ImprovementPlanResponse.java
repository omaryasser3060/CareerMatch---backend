package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementPlanResponse {

    private List<RecommendationResponse> recommendations;
    private Summary summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationResponse {
        private String gapId;
        private String gapName;
        private String gapCategory;
        private String requiredOrPreferred;
        private Integer importanceWeight;
        private String jobEvidence;
        private String cvEvidence;
        private List<String> relatedExistingStrengths;
        private String recommendedAction;
        private String deliverable;
        private String estimatedEffort;
        private Integer expectedScoreGain;
        private Integer priorityScore;
        private String priorityLabel;
        private String confidence;
        private List<ResourceResponse> resources;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceResponse {
        private String title;
        private String url;
        private String type;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalGaps;
        private Integer highPriority;
        private Integer mediumPriority;
        private Integer lowPriority;
        private Integer expectedScoreGainTotal;
    }
}