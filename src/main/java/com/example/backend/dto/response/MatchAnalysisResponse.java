package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchAnalysisResponse {

    private String matchId;
    private Integer overallMatchScore;
    private ScoreBreakdown scoreBreakdown;
    private List<SkillMatchResponse> matchedRequiredSkills;
    private List<SkillMatchResponse> partialMatches;
    private List<SkillMatchResponse> missingRequiredSkills;
    private List<SkillMatchResponse> matchedPreferredSkills;
    private List<SkillMatchResponse> missingPreferredSkills;
    private List<String> strengths;
    private Map<String, EvidenceResponse> evidence;
    private ImprovementPlanResponse improvementPlan;
    private String extractionConfidence;
    private Boolean humanReviewFlag;
    private LocalDateTime createdAt;
    private String jobTitle;
    private String companyName;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreBreakdown {
        private Integer requiredSkills;
        private Integer experience;
        private Integer projects;
        private Integer education;
        private Integer preferredSkills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMatchResponse {
        private String skill;
        private Double matchValue;
        private String status;
        private EvidenceResponse evidence;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenceResponse {
        private String cvEvidence;
        private String jobEvidence;
        private Double similarityScore;
        private String explanation;
    }
}