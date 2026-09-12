package com.example.backend.service.ai;

import com.example.backend.dto.ai.CandidateProfileDto;
import com.example.backend.dto.ai.JobRequirementsDto;
import com.example.backend.dto.ai.RecommendationDto;
import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.dto.response.MatchAnalysisResponse;
import com.example.backend.model.CV;
import com.example.backend.model.Job;
import com.example.backend.repository.JobRepository;
import com.example.backend.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {

    private final LLMService llmService;
    private final EmbeddingService embeddingService;
    private final SkillNormalizationService skillNormalizationService;
    private final ScoringService scoringService;
    private final JobRepository jobRepository;

    public MatchAnalysisResponse analyzeMatch(CV cv, Job job) {
        log.info("Running AI analysis for CV: {}, Job: {}", cv.getId(), job.getId());

        try {
            // 1. Load the stored candidate profile first.
            // The CV is parsed once during CV processing and reused here.
            CandidateProfileDto candidateProfile = null;

            if (cv.getProfileJson() != null && !cv.getProfileJson().isBlank()) {
                try {
                    candidateProfile =
                            JsonUtils.fromJson(
                                    cv.getProfileJson(),
                                    CandidateProfileDto.class
                            );

                    log.info("Successfully loaded pre-parsed CV profile from database.");

                } catch (Exception e) {
                    log.warn(
                            "Failed to parse existing profile JSON, falling back to LLM extraction.",
                            e
                    );
                }
            }

            // Fallback only when the stored profile is unavailable/invalid.
            if (candidateProfile == null) {
                log.info("Calling LLM to extract candidate profile...");

                candidateProfile =
                        llmService.extractCandidateProfile(
                                cv.getRawText()
                        );
            }

            if (candidateProfile == null) {
                throw new IllegalStateException(
                        "Candidate profile could not be loaded or extracted."
                );
            }

            // 2. Analyze the selected job only when Match is requested.
            // Adzuna ingestion stores the job as raw data and does not call Groq.
            JobRequirementsDto jobRequirements = loadStoredJobRequirements(job);

            if (jobRequirements != null) {
                log.info("Loaded stored job requirements from database for Job: {}", job.getId());
            } else {
                log.info("No stored job requirements found. Analyzing selected job via Groq...");
                jobRequirements =
                        llmService.extractJobRequirements(
                                job.getTitle(),
                                job.getDescription()
                        );

                if (jobRequirements != null) {
                    persistJobRequirements(job, jobRequirements);
                }
            }

            if (jobRequirements == null) {
                throw new IllegalStateException(
                        "Job requirements could not be extracted."
                );
            }

            // 3. Normalize skills.
            List<String> candidateSkills =
                    normalize(candidateProfile.getSkills());

            List<String> requiredSkills =
                    normalize(jobRequirements.getRequiredSkills());

            List<String> preferredSkills =
                    normalize(jobRequirements.getPreferredSkills());

            // 4. Semantic comparison uses the complete candidate profile
            // against the complete structured/raw job profile.
            double semanticSimilarity =
                    embeddingService.cosineSimilarity(
                            buildCandidateSemanticText(candidateProfile),
                            buildJobSemanticText(job, jobRequirements)
                    );

            // 5. Calculate the final score deterministically in the backend.
            // Groq does not calculate or modify the numeric score.
            int matchScore =
                    scoringService.calculateMatchScore(
                            candidateProfile,
                            jobRequirements,
                            candidateSkills,
                            requiredSkills,
                            preferredSkills,
                            semanticSimilarity
                    );

            // 6. Find matched/missing required and preferred skills.
            List<String> matchedRequired =
                    scoringService.findMatched(
                            candidateSkills,
                            requiredSkills
                    );

            List<String> missingRequired =
                    scoringService.findMissing(
                            candidateSkills,
                            requiredSkills
                    );

            List<String> matchedPreferred =
                    scoringService.findMatched(
                            candidateSkills,
                            preferredSkills
                    );

            List<String> missingPreferred =
                    scoringService.findMissing(
                            candidateSkills,
                            preferredSkills
                    );

            // 7. Groq generates the explanation/recommendations using
            // the already calculated deterministic score.
            RecommendationDto recommendations =
                    llmService.generateRecommendations(
                            candidateProfile,
                            jobRequirements,
                            matchedRequired,
                            missingRequired,
                            matchedPreferred,
                            missingPreferred,
                            matchScore
                    );

            // 8. Build response.
            return buildResponse(
                    matchScore,
                    candidateSkills,
                    requiredSkills,
                    preferredSkills,
                    matchedRequired,
                    missingRequired,
                    matchedPreferred,
                    missingPreferred,
                    semanticSimilarity,
                    candidateProfile,
                    recommendations
            );

        } catch (Exception e) {
            log.error(
                    "AI analysis failed for CV: {}, Job: {}",
                    cv.getId(),
                    job.getId(),
                    e
            );

            throw new RuntimeException(
                    "AI analysis failed: " + e.getMessage(),
                    e
            );
        }
    }

    private MatchAnalysisResponse buildResponse(
            int matchScore,
            List<String> candidateSkills,
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> matchedRequired,
            List<String> missingRequired,
            List<String> matchedPreferred,
            List<String> missingPreferred,
            double semanticSimilarity,
            CandidateProfileDto candidateProfile,
            RecommendationDto recommendations
    ) {

        MatchAnalysisResponse.ScoreBreakdown breakdown =
                MatchAnalysisResponse.ScoreBreakdown.builder()
                        .requiredSkills(
                                percentage(
                                        matchedRequired.size(),
                                        requiredSkills.size()
                                )
                        )
                        .preferredSkills(
                                percentage(
                                        matchedPreferred.size(),
                                        preferredSkills.size()
                                )
                        )
                        .semanticSimilarityAvg(
                                semanticSimilarity
                        )
                        .build();

        List<MatchAnalysisResponse.SkillMatchResponse>
                matchedRequiredResponses =
                matchedRequired.stream()
                        .map(skill ->
                                MatchAnalysisResponse.SkillMatchResponse
                                        .builder()
                                        .skill(skill)
                                        .status("MATCHED")
                                        .matchValue(1.0)
                                        .build()
                        )
                        .toList();

        List<MatchAnalysisResponse.SkillMatchResponse>
                missingRequiredResponses =
                missingRequired.stream()
                        .map(skill ->
                                MatchAnalysisResponse.SkillMatchResponse
                                        .builder()
                                        .skill(skill)
                                        .status("MISSING")
                                        .matchValue(0.0)
                                        .build()
                        )
                        .toList();

        List<MatchAnalysisResponse.SkillMatchResponse>
                matchedPreferredResponses =
                matchedPreferred.stream()
                        .map(skill ->
                                MatchAnalysisResponse.SkillMatchResponse
                                        .builder()
                                        .skill(skill)
                                        .status("MATCHED")
                                        .matchValue(1.0)
                                        .build()
                        )
                        .toList();

        List<MatchAnalysisResponse.SkillMatchResponse>
                missingPreferredResponses =
                missingPreferred.stream()
                        .map(skill ->
                                MatchAnalysisResponse.SkillMatchResponse
                                        .builder()
                                        .skill(skill)
                                        .status("MISSING")
                                        .matchValue(0.0)
                                        .build()
                        )
                        .toList();

        String confidence =
                candidateProfile.getConfidence() != null
                        ? candidateProfile.getConfidence()
                        : "MEDIUM";

        boolean humanReviewFlag =
                "low".equalsIgnoreCase(confidence);

        return MatchAnalysisResponse.builder()
                .overallMatchScore(matchScore)
                .scoreBreakdown(breakdown)
                .matchedRequiredSkills(matchedRequiredResponses)
                .missingRequiredSkills(missingRequiredResponses)
                .matchedPreferredSkills(matchedPreferredResponses)
                .missingPreferredSkills(missingPreferredResponses)
                .strengths(
                        recommendations != null
                                && recommendations.getStrengths() != null
                                ? recommendations.getStrengths()
                                : new ArrayList<>()
                )
                .improvementPlan(
                        buildImprovementPlan(recommendations)
                )
                .extractionConfidence(confidence)
                .humanReviewFlag(humanReviewFlag)
                .build();
    }

    private JobRequirementsDto loadStoredJobRequirements(Job job) {
        if (job == null
                || job.getRequirementsJson() == null
                || job.getRequirementsJson().isBlank()) {
            return null;
        }

        try {
            return JsonUtils.fromJson(
                    job.getRequirementsJson(),
                    JobRequirementsDto.class
            );
        } catch (Exception e) {
            log.warn(
                    "Stored job requirements JSON is invalid for Job: {}. Re-analyzing job.",
                    job.getId(),
                    e
            );
            return null;
        }
    }

    private void persistJobRequirements(
            Job job,
            JobRequirementsDto jobRequirements
    ) {
        try {
            job.setRequirementsJson(JsonUtils.toJson(jobRequirements));
            job.setRequiredSkillsJson(
                    JsonUtils.toJson(jobRequirements.getRequiredSkills())
            );
            job.setPreferredSkillsJson(
                    JsonUtils.toJson(jobRequirements.getPreferredSkills())
            );
            job.setExpectedExperienceMonths(
                    jobRequirements.getExperienceMonths()
            );

            jobRepository.save(job);

            log.info(
                    "Stored analyzed job requirements for Job: {}",
                    job.getId()
            );
        } catch (Exception e) {
            // Matching should still succeed even if caching the AI result fails.
            log.warn(
                    "Could not persist analyzed job requirements for Job: {}. Continuing match analysis.",
                    job.getId(),
                    e
            );
        }
    }

    private String buildCandidateSemanticText(
            CandidateProfileDto candidate
    ) {
        return String.join("\n",
                "Candidate Skills: " + safe(candidate.getSkills()),
                "Experience Summary: " + safe(candidate.getExperienceSummary()),
                "Experience Months: " + String.valueOf(candidate.getExperienceMonths()),
                "Past Titles: " + safe(candidate.getPastTitles()),
                "Projects: " + JsonUtils.toJson(candidate.getProjects()),
                "Education: " + JsonUtils.toJson(candidate.getEducation()),
                "Certifications: " + JsonUtils.toJson(candidate.getCertifications()),
                "Languages: " + JsonUtils.toJson(candidate.getLanguages())
        );
    }

    private String buildJobSemanticText(
            Job job,
            JobRequirementsDto requirements
    ) {
        return String.join("\n",
                "Job Title: " + safe(job.getTitle()),
                "Job Description: " + safe(job.getDescription()),
                "Required Skills: " + JsonUtils.toJson(requirements.getRequiredSkills()),
                "Preferred Skills: " + JsonUtils.toJson(requirements.getPreferredSkills()),
                "Experience Level: " + safe(requirements.getExperienceLevel()),
                "Experience Months: " + String.valueOf(requirements.getExperienceMonths()),
                "Responsibilities: " + JsonUtils.toJson(requirements.getResponsibilities()),
                "Education Requirements: " + JsonUtils.toJson(requirements.getEducationRequirements()),
                "Certification Requirements: " + JsonUtils.toJson(requirements.getCertificationRequirements()),
                "Language Requirements: " + JsonUtils.toJson(requirements.getLanguageRequirements()),
                "Other Requirements: " + JsonUtils.toJson(requirements.getOtherRequirements())
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private ImprovementPlanResponse buildImprovementPlan(
            RecommendationDto recommendations
    ) {

        List<ImprovementPlanResponse.RecommendationResponse>
                items =
                new ArrayList<>();

        if (recommendations != null
                && recommendations.getRecommendations() != null) {

            for (
                    RecommendationDto.RecommendationItemDto item
                    : recommendations.getRecommendations()
            ) {

                if (item == null
                        || item.getGapName() == null
                        || item.getGapName().isBlank()) {
                    continue;
                }

                int expectedGain =
                        item.getExpectedScoreGain() != null
                                ? Math.max(
                                        0,
                                        Math.min(
                                                100,
                                                item.getExpectedScoreGain()
                                        )
                                )
                                : 0;

                String priority =
                        normalizePriority(
                                item.getPriority()
                        );

                String confidence =
                        item.getConfidence() != null
                                && !item.getConfidence().isBlank()
                                ? item.getConfidence()
                                : "MEDIUM";

                List<ImprovementPlanResponse.ResourceResponse>
                        resources =
                        item.getResources() == null
                                ? new ArrayList<>()
                                : item.getResources()
                                .stream()
                                .filter(resource -> resource != null)
                                .map(resource ->
                                        ImprovementPlanResponse.ResourceResponse
                                                .builder()
                                                .title(resource.getTitle())
                                                .url(resource.getUrl())
                                                .type(resource.getType())
                                                .description(resource.getDescription())
                                                .build()
                                )
                                .toList();

                items.add(
                        ImprovementPlanResponse.RecommendationResponse
                                .builder()
                                .gapName(item.getGapName())
                                .gapCategory(
                                        item.getGapCategory() != null
                                                ? item.getGapCategory()
                                                : "OTHER"
                                )
                                .requiredOrPreferred(
                                        item.getRequiredOrPreferred() != null
                                                ? item.getRequiredOrPreferred()
                                                .toUpperCase(Locale.ROOT)
                                                : "REQUIRED"
                                )
                                .importanceWeight(
                                        item.getImportanceWeight() != null
                                                ? Math.max(
                                                        1,
                                                        Math.min(
                                                                10,
                                                                item.getImportanceWeight()
                                                        )
                                                )
                                                : 5
                                )
                                .jobEvidence(item.getJobEvidence())
                                .cvEvidence(item.getCvEvidence())
                                .relatedExistingStrengths(
                                        item.getRelatedExistingStrengths()
                                )
                                .recommendedAction(
                                        item.getRecommendedAction() != null
                                                && !item.getRecommendedAction().isBlank()
                                                ? item.getRecommendedAction()
                                                : "Build practical experience with this skill."
                                )
                                .deliverable(item.getDeliverable())
                                .estimatedEffort(
                                        item.getEstimatedEffort() != null
                                                && !item.getEstimatedEffort().isBlank()
                                                ? item.getEstimatedEffort()
                                                : "Not specified"
                                )
                                .expectedScoreGain(expectedGain)
                                .priorityScore(priorityScore(priority))
                                .priorityLabel(priority)
                                .confidence(confidence)
                                .resources(resources)
                                .status("PENDING")
                                .build()
                );
            }
        }

        int high = 0;
        int medium = 0;
        int low = 0;
        int totalGain = 0;

        for (
                ImprovementPlanResponse.RecommendationResponse item
                : items
        ) {

            totalGain +=
                    item.getExpectedScoreGain() != null
                            ? item.getExpectedScoreGain()
                            : 0;

            switch (item.getPriorityLabel()) {
                case "VERY_HIGH", "HIGH" ->
                        high++;

                case "MEDIUM" ->
                        medium++;

                default ->
                        low++;
            }
        }

        return ImprovementPlanResponse
                .builder()
                .recommendations(items)
                .summary(
                        ImprovementPlanResponse
                                .Summary
                                .builder()
                                .totalGaps(items.size())
                                .highPriority(high)
                                .mediumPriority(medium)
                                .lowPriority(low)
                                .expectedScoreGainTotal(totalGain)
                                .build()
                )
                .build();
    }

    private String normalizePriority(
            String priority
    ) {

        if (priority == null
                || priority.isBlank()) {
            return "MEDIUM";
        }

        String normalized =
                priority
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace('-', '_')
                        .replace(' ', '_');

        return switch (normalized) {
            case "VERY_HIGH",
                 "HIGH",
                 "MEDIUM",
                 "LOW" ->
                    normalized;

            default ->
                    "MEDIUM";
        };
    }

    private int priorityScore(
            String priority
    ) {

        return switch (priority) {
            case "VERY_HIGH" ->
                    100;

            case "HIGH" ->
                    80;

            case "MEDIUM" ->
                    60;

            default ->
                    40;
        };
    }

    private List<String> normalize(
            List<String> skills
    ) {

        if (skills == null
                || skills.isEmpty()) {
            return List.of();
        }

        return skillNormalizationService
                .normalizeAll(skills)
                .stream()
                .filter(
                        skill ->
                                skill != null
                                        && !skill.isBlank()
                )
                .distinct()
                .toList();
    }

    private int percentage(
            int matched,
            int total
    ) {

        if (total <= 0) {
            return 0;
        }

        return (int) Math.round(
                ((double) matched / total) * 100
        );
    }
}
