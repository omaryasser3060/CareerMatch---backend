package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Match analysis response")
public class MatchAnalysisResponse {

    @Schema(description = "Match result ID")
    private String matchId;

    @Schema(description = "CV ID")
    private String cvId;

    @Schema(description = "Job ID")
    private String jobId;

    // ============================================================
    // Overall AI Assessment
    // ============================================================

    @Schema(
            description = "Overall deterministic match score (0-100)",
            example = "82"
    )
    private Integer overallMatchScore;

    @Schema(
            description = "AI-generated match level aligned with the deterministic score",
            example = "STRONG"
    )
    private String matchLevel;

    @Schema(
            description = "AI-generated overall assessment of the candidate against the job"
    )
    private String overallAssessment;

    @Schema(
            description = "AI-generated concise summary of the match"
    )
    private String summary;

    // ============================================================
    // Score Breakdown
    // ============================================================

    @Schema(description = "Score breakdown by category")
    private ScoreBreakdown scoreBreakdown;

    // ============================================================
    // Skills
    // ============================================================

    @Schema(description = "Matched required skills")
    private List<SkillMatchResponse> matchedRequiredSkills;

    @Schema(description = "Partially matched skills")
    private List<SkillMatchResponse> partialMatches;

    @Schema(description = "Missing required skills")
    private List<SkillMatchResponse> missingRequiredSkills;

    @Schema(description = "Matched preferred skills")
    private List<SkillMatchResponse> matchedPreferredSkills;

    @Schema(description = "Missing preferred skills")
    private List<SkillMatchResponse> missingPreferredSkills;

    // ============================================================
    // Evidence & Strengths
    // ============================================================

    @Schema(description = "Candidate strengths")
    private List<String> strengths;

    @Schema(description = "Evidence for matches/mismatches")
    private Map<String, EvidenceResponse> evidence;

    // ============================================================
    // Improvement Plan
    // ============================================================

    @Schema(description = "AI-generated improvement plan")
    private ImprovementPlanResponse improvementPlan;

    // ============================================================
    // Extraction / Review Metadata
    // ============================================================

    @Schema(
            description = "Extraction confidence",
            example = "HIGH"
    )
    private String extractionConfidence;

    @Schema(
            description = "Whether human review is flagged",
            example = "false"
    )
    private Boolean humanReviewFlag;

    // ============================================================
    // Job Information
    // ============================================================

    @Schema(description = "Job title (denormalized)")
    private String jobTitle;

    @Schema(description = "Company name (denormalized)")
    private String companyName;

    @Schema(description = "Analysis timestamp")
    private LocalDateTime createdAt;

    // ============================================================
    // Nested DTOs
    // ============================================================

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Score breakdown by category")
    public static class ScoreBreakdown {

        @Schema(
                description = "Required skills score (0-100)",
                example = "75"
        )
        private Integer requiredSkills;

        @Schema(
                description = "Experience score (0-100)",
                example = "80"
        )
        private Integer experience;

        @Schema(
                description = "Projects and evidence score (0-100)",
                example = "70"
        )
        private Integer projects;

        @Schema(
                description = "Education score (0-100)",
                example = "90"
        )
        private Integer education;

        @Schema(
                description = "Preferred skills score (0-100)",
                example = "50"
        )
        private Integer preferredSkills;

        @Schema(
                description = "Semantic similarity score (0-1)",
                example = "0.88"
        )
        private Double semanticSimilarityAvg;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Skill match details")
    public static class SkillMatchResponse {

        @Schema(
                description = "Skill name",
                example = "Python"
        )
        private String skill;

        @Schema(
                description = "Match value (0-1)",
                example = "0.95"
        )
        private Double matchValue;

        @Schema(
                description = "Match status",
                example = "MATCHED"
        )
        private String status;

        @Schema(
                description = "Skill category",
                example = "LANGUAGE"
        )
        private String category;

        @Schema(description = "Evidence for this match")
        private EvidenceResponse evidence;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Evidence for a match or mismatch")
    public static class EvidenceResponse {

        @Schema(description = "Evidence from CV")
        private String cvEvidence;

        @Schema(description = "Evidence from job description")
        private String jobEvidence;

        @Schema(
                description = "Semantic similarity score (0-1)",
                example = "0.88"
        )
        private Double similarityScore;

        @Schema(description = "Explanation")
        private String explanation;
    }
}