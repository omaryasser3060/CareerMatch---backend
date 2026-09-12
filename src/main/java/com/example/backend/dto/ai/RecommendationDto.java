package com.example.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationDto {

    @JsonProperty("overall_assessment")
    private String overallAssessment;

    @JsonProperty("match_level")
    private String matchLevel;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("strengths")
    private List<String> strengths;

    @JsonProperty("recommendations")
    private List<RecommendationItemDto> recommendations;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendationItemDto {

        @JsonProperty("gap_name")
        private String gapName;

        @JsonProperty("gap_category")
        private String gapCategory;

        @JsonProperty("required_or_preferred")
        private String requiredOrPreferred;

        @JsonProperty("importance_weight")
        private Integer importanceWeight;

        @JsonProperty("job_evidence")
        private String jobEvidence;

        @JsonProperty("cv_evidence")
        private String cvEvidence;

        @JsonProperty("related_existing_strengths")
        private List<String> relatedExistingStrengths;

        @JsonProperty("recommended_action")
        private String recommendedAction;

        @JsonProperty("deliverable")
        private String deliverable;

        @JsonProperty("estimated_effort")
        private String estimatedEffort;

        @JsonProperty("expected_score_gain")
        private Integer expectedScoreGain;

        @JsonProperty("priority")
        private String priority;

        @JsonProperty("resources")
        private List<ResourceDto> resources;

        @JsonProperty("confidence")
        private String confidence;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResourceDto {

        private String title;

        private String url;

        private String type;

        private String description;
    }
}