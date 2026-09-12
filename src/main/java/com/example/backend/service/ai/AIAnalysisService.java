package com.example.backend.service.ai;

import com.example.backend.dto.ai.CandidateProfileDto;
import com.example.backend.dto.ai.JobRequirementsDto;
import com.example.backend.dto.ai.RecommendationDto;
import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.dto.response.MatchAnalysisResponse;
import com.example.backend.model.CV;
import com.example.backend.model.Job;
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

    public MatchAnalysisResponse analyzeMatch(CV cv, Job job) {
        log.info("Running AI analysis for CV: {}, Job: {}", cv.getId(), job.getId());

        try {
            // 1. Extract candidate profile (LLM)
            CandidateProfileDto candidateProfile = llmService.extractCandidateProfile(cv.getRawText());

            // 2. Extract job requirements (LLM)
            JobRequirementsDto jobRequirements = llmService.extractJobRequirements(job.getDescription());

            // 3. Normalize skills
            List<String> candidateSkills = skillNormalizationService.normalizeAll(candidateProfile.getSkills());
            List<String> requiredSkills = skillNormalizationService.normalizeAll(jobRequirements.getRequiredSkills());
            List<String> preferredSkills = skillNormalizationService.normalizeAll(jobRequirements.getPreferredSkills());

            // 4. Semantic comparison
            double semanticSimilarity = embeddingService.cosineSimilarity(
                    String.join(" ", candidateSkills),
                    String.join(" ", requiredSkills)
            );

            // 5. Calculate score
            int matchScore = scoringService.calculateMatchScore(
                    candidateSkills, requiredSkills, preferredSkills, semanticSimilarity
            );

            // 6. Find matched/missing
            List<String> matchedRequired = scoringService.findMatched(candidateSkills, requiredSkills);
            List<String> missingRequired = scoringService.findMissing(candidateSkills, requiredSkills);
            List<String> matchedPreferred = scoringService.findMatched(candidateSkills, preferredSkills);
            List<String> missingPreferred = scoringService.findMissing(candidateSkills, preferredSkills);

            // 7. Generate recommendations (LLM)
            RecommendationDto recommendations = llmService.generateRecommendations(
                    candidateProfile, jobRequirements,
                    matchedRequired, missingRequired, matchScore
            );

            // 8. Build response
            return buildResponse(
                    matchScore, candidateSkills, requiredSkills, preferredSkills,
                    matchedRequired, missingRequired, matchedPreferred, missingPreferred,
                    semanticSimilarity, candidateProfile, recommendations
            );

        } catch (Exception e) {
            log.error("AI analysis failed for CV: {}, Job: {}", cv.getId(), job.getId(), e);
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
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
        MatchAnalysisResponse.ScoreBreakdown breakdown = MatchAnalysisResponse.ScoreBreakdown.builder()
                .requiredSkills((int) Math.round(
                        (double) matchedRequired.size() / Math.max(1, requiredSkills.size()) * 100))
                .preferredSkills((int) Math.round(
                        (double) matchedPreferred.size() / Math.max(1, preferredSkills.size()) * 100))
                .semanticSimilarityAvg(semanticSimilarity)
                .build();

        List<MatchAnalysisResponse.SkillMatchResponse> matchedRequiredResponses =
                matchedRequired.stream()
                        .map(s -> MatchAnalysisResponse.SkillMatchResponse.builder()
                                .skill(s)
                                .status("MATCHED")
                                .matchValue(1.0)
                                .build())
                        .toList();

        List<MatchAnalysisResponse.SkillMatchResponse> missingRequiredResponses =
                missingRequired.stream()
                        .map(s -> MatchAnalysisResponse.SkillMatchResponse.builder()
                                .skill(s)
                                .status("MISSING")
                                .matchValue(0.0)
                                .build())
                        .toList();

        List<MatchAnalysisResponse.SkillMatchResponse> matchedPreferredResponses =
                matchedPreferred.stream()
                        .map(s -> MatchAnalysisResponse.SkillMatchResponse.builder()
                                .skill(s)
                                .status("MATCHED")
                                .matchValue(1.0)
                                .build())
                        .toList();

        List<MatchAnalysisResponse.SkillMatchResponse> missingPreferredResponses =
                missingPreferred.stream()
                        .map(s -> MatchAnalysisResponse.SkillMatchResponse.builder()
                                .skill(s)
                                .status("MISSING")
                                .matchValue(0.0)
                                .build())
                        .toList();

        // Determine confidence
        String confidence = candidateProfile.getConfidence() != null
                ? candidateProfile.getConfidence()
                : "MEDIUM";
        boolean humanReviewFlag = "low".equalsIgnoreCase(confidence);

        return MatchAnalysisResponse.builder()
                .overallMatchScore(matchScore)
                .scoreBreakdown(breakdown)
                .matchedRequiredSkills(matchedRequiredResponses)
                .missingRequiredSkills(missingRequiredResponses)
                .matchedPreferredSkills(matchedPreferredResponses)
                .missingPreferredSkills(missingPreferredResponses)
                .strengths(recommendations.getStrengths() != null
                        ? recommendations.getStrengths()
                        : new ArrayList<>())
                .improvementPlan(buildImprovementPlan(recommendations))
                .extractionConfidence(confidence)
                .humanReviewFlag(humanReviewFlag)
                .build();
    }

    private ImprovementPlanResponse buildImprovementPlan(RecommendationDto recommendations) {
        List<ImprovementPlanResponse.RecommendationResponse> items = new ArrayList<>();

        if (recommendations != null && recommendations.getRecommendations() != null) {
            for (RecommendationDto.RecommendationItemDto item : recommendations.getRecommendations()) {
                if (item == null || item.getGapName() == null || item.getGapName().isBlank()) {
                    continue;
                }

                int expectedGain = item.getExpectedScoreGain() != null
                        ? Math.max(0, Math.min(100, item.getExpectedScoreGain()))
                        : 0;
                String priority = normalizePriority(item.getPriority());
                String confidence = item.getConfidence() != null && !item.getConfidence().isBlank()
                        ? item.getConfidence()
                        : "MEDIUM";

                List<ImprovementPlanResponse.ResourceResponse> resources = item.getResources() == null
                        ? new ArrayList<>()
                        : item.getResources().stream()
                        .filter(resource -> resource != null)
                        .map(resource -> ImprovementPlanResponse.ResourceResponse.builder()
                                .title(resource.getTitle())
                                .url(resource.getUrl())
                                .type(resource.getType())
                                .description(resource.getDescription())
                                .build())
                        .toList();

                items.add(ImprovementPlanResponse.RecommendationResponse.builder()
                        .gapName(item.getGapName())
                        .gapCategory(item.getGapCategory() != null ? item.getGapCategory() : "OTHER")
                        .requiredOrPreferred("REQUIRED")
                        .importanceWeight(10)
                        .recommendedAction(item.getRecommendedAction() != null && !item.getRecommendedAction().isBlank()
                                ? item.getRecommendedAction()
                                : "Build practical experience with this skill.")
                        .deliverable(item.getDeliverable())
                        .estimatedEffort(item.getEstimatedEffort() != null && !item.getEstimatedEffort().isBlank()
                                ? item.getEstimatedEffort()
                                : "Not specified")
                        .expectedScoreGain(expectedGain)
                        .priorityScore(priorityScore(priority))
                        .priorityLabel(priority)
                        .confidence(confidence)
                        .resources(resources)
                        .status("PENDING")
                        .build());
            }
        }

        int high = 0;
        int medium = 0;
        int low = 0;
        int totalGain = 0;

        for (ImprovementPlanResponse.RecommendationResponse item : items) {
            totalGain += item.getExpectedScoreGain() != null ? item.getExpectedScoreGain() : 0;
            switch (item.getPriorityLabel()) {
                case "VERY_HIGH", "HIGH" -> high++;
                case "MEDIUM" -> medium++;
                default -> low++;
            }
        }

        return ImprovementPlanResponse.builder()
                .recommendations(items)
                .summary(ImprovementPlanResponse.Summary.builder()
                        .totalGaps(items.size())
                        .highPriority(high)
                        .mediumPriority(medium)
                        .lowPriority(low)
                        .expectedScoreGainTotal(totalGain)
                        .build())
                .build();
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "MEDIUM";
        }

        String normalized = priority.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "VERY_HIGH", "HIGH", "MEDIUM", "LOW" -> normalized;
            default -> "MEDIUM";
        };
    }

    private int priorityScore(String priority) {
        return switch (priority) {
            case "VERY_HIGH" -> 100;
            case "HIGH" -> 80;
            case "MEDIUM" -> 60;
            default -> 40;
        };
    }

}
