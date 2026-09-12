package com.example.backend.service;

import com.example.backend.dto.response.MatchAnalysisResponse;
import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.dto.response.MatchHistoryResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.ValidationException;
import com.example.backend.model.CV;
import com.example.backend.model.Job;
import com.example.backend.model.ImprovementRecommendation;
import com.example.backend.model.MatchResult;
import com.example.backend.model.User;
import com.example.backend.repository.ImprovementRecommendationRepository;
import com.example.backend.repository.MatchResultRepository;
import com.example.backend.service.ai.AIAnalysisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchResultRepository matchResultRepository;
    private final ImprovementRecommendationRepository recommendationRepository;
    private final UserService userService;
    private final CVService cvService;
    private final JobService jobService;
    private final AIAnalysisService aiAnalysisService;
    private final ObjectMapper objectMapper;

    // ============================================================
    // Analyze Match
    // ============================================================

    @Transactional
    public MatchAnalysisResponse analyzeMatch(String userId, String cvId, String jobId) {
        log.info("Match analysis request: userId={}, cvId={}, jobId={}", userId, cvId, jobId);

        User user = userService.findUserById(userId);
        CV cv = cvService.getCVEntity(userId, cvId);
        Job job = jobService.getJobEntity(jobId);

        if (!cv.isParsed()) {
            throw new ValidationException("CV is not parsed yet. Please wait for processing.");
        }

        // Check if match already exists
        if (matchResultRepository.existsByCvIdAndJobId(cvId, jobId)) {
            log.info("Match already exists for cvId={}, jobId={}", cvId, jobId);
            MatchResult existing = matchResultRepository.findByCvIdAndJobId(cvId, jobId)
                    .orElseThrow();
            return buildMatchAnalysisResponse(existing);
        }

        // Run AI analysis
        MatchAnalysisResponse analysis = aiAnalysisService.analyzeMatch(cv, job);

        // Save match result
        MatchResult matchResult = MatchResult.builder()
                .user(user)
                .cv(cv)
                .job(job)
                .overallMatchScore(analysis.getOverallMatchScore())
                .scoreBreakdownJson(toJson(analysis.getScoreBreakdown()))
                .matchedSkillsJson(toJson(analysis.getMatchedRequiredSkills()))
                .missingSkillsJson(toJson(analysis.getMissingRequiredSkills()))
                .strengthsJson(toJson(analysis.getStrengths()))
                .improvementPlanJson(toJson(analysis.getImprovementPlan()))
                .evidenceJson(toJson(analysis.getEvidence()))
                .extractionConfidence(analysis.getExtractionConfidence())
                .humanReviewFlag(analysis.getHumanReviewFlag())
                .jobTitle(job.getTitle())
                .companyName(job.getCompany())
                .build();

        matchResult = matchResultRepository.save(matchResult);
        persistRecommendations(matchResult, analysis.getImprovementPlan());

        analysis.setMatchId(matchResult.getId());
        analysis.setCvId(cvId);
        analysis.setJobId(jobId);

        // The database rows are the source of truth for the improvement plan.
        analysis.setImprovementPlan(buildImprovementPlanResponse(matchResult));

        log.info("Match analysis completed: matchId={}, score={}",
                matchResult.getId(), matchResult.getOverallMatchScore());

        return analysis;
    }

    // ============================================================
    // Get Match Result
    // ============================================================

    @Transactional(readOnly = true)
    public MatchAnalysisResponse getMatchResult(String userId, String matchId) {
        MatchResult matchResult = matchResultRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        return buildMatchAnalysisResponse(matchResult);
    }

    // ============================================================
    // Get Match History
    // ============================================================

    @Transactional(readOnly = true)
    public MatchHistoryResponse getMatchHistory(
            String userId,
            Pageable pageable,
            Integer minScore,
            Integer maxScore,
            String jobTitle,
            String companyName
    ) {
        Page<MatchResult> matchPage = matchResultRepository.filterMatches(
                userId, minScore, maxScore, jobTitle, companyName, null, null, pageable
        );

        List<MatchHistoryResponse.MatchSummaryResponse> summaries = matchPage.getContent()
                .stream()
                .map(this::buildMatchSummary)
                .toList();

        return MatchHistoryResponse.builder()
                .results(summaries)
                .page(matchPage.getNumber())
                .totalPages(matchPage.getTotalPages())
                .totalCount(matchPage.getTotalElements())
                .pageSize(matchPage.getSize())
                .hasNext(matchPage.hasNext())
                .hasPrevious(matchPage.hasPrevious())
                .build();
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteMatchResult(String userId, String matchId) {
        MatchResult matchResult = matchResultRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        matchResultRepository.delete(matchResult);
        log.info("Match result deleted: {}", matchId);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private MatchAnalysisResponse buildMatchAnalysisResponse(MatchResult matchResult) {
        try {
            MatchAnalysisResponse response = MatchAnalysisResponse.builder()
                    .matchId(matchResult.getId())
                    .cvId(matchResult.getCv().getId())
                    .jobId(matchResult.getJob().getId())
                    .overallMatchScore(matchResult.getOverallMatchScore())
                    .extractionConfidence(matchResult.getExtractionConfidence())
                    .humanReviewFlag(matchResult.isHumanReviewFlag())
                    .jobTitle(matchResult.getJobTitle())
                    .companyName(matchResult.getCompanyName())
                    .createdAt(matchResult.getCreatedAt())
                    .build();

            // Parse score breakdown
            if (matchResult.getScoreBreakdownJson() != null
                    && !matchResult.getScoreBreakdownJson().equals("null")) {
                response.setScoreBreakdown(objectMapper.readValue(
                        matchResult.getScoreBreakdownJson(),
                        MatchAnalysisResponse.ScoreBreakdown.class
                ));
            }

            // Parse strengths
            if (matchResult.getStrengthsJson() != null
                    && !matchResult.getStrengthsJson().equals("null")) {
                response.setStrengths(objectMapper.readValue(
                        matchResult.getStrengthsJson(),
                        new TypeReference<List<String>>() {}
                ));
            }

            // Parse matched required skills
            if (matchResult.getMatchedSkillsJson() != null
                    && !matchResult.getMatchedSkillsJson().equals("null")) {
                response.setMatchedRequiredSkills(objectMapper.readValue(
                        matchResult.getMatchedSkillsJson(),
                        new TypeReference<List<MatchAnalysisResponse.SkillMatchResponse>>() {}
                ));
            }

            // Parse missing required skills
            if (matchResult.getMissingSkillsJson() != null
                    && !matchResult.getMissingSkillsJson().equals("null")) {
                response.setMissingRequiredSkills(objectMapper.readValue(
                        matchResult.getMissingSkillsJson(),
                        new TypeReference<List<MatchAnalysisResponse.SkillMatchResponse>>() {}
                ));
            }

            // Improvement recommendations are persisted as normalized rows.
            response.setImprovementPlan(buildImprovementPlanResponse(matchResult));

            // Restore evidence when it was persisted by the analysis pipeline.
            if (matchResult.getEvidenceJson() != null
                    && !matchResult.getEvidenceJson().equals("null")) {
                response.setEvidence(objectMapper.readValue(
                        matchResult.getEvidenceJson(),
                        new TypeReference<java.util.Map<String, MatchAnalysisResponse.EvidenceResponse>>() {}
                ));
            }

            return response;
        } catch (Exception e) {
            log.error("Failed to parse match result: {}", matchResult.getId(), e);
            throw new RuntimeException("Failed to parse match result", e);
        }
    }

    private MatchHistoryResponse.MatchSummaryResponse buildMatchSummary(MatchResult matchResult) {
        return MatchHistoryResponse.MatchSummaryResponse.builder()
                .matchId(matchResult.getId())
                .overallMatchScore(matchResult.getOverallMatchScore())
                .jobTitle(matchResult.getJobTitle())
                .companyName(matchResult.getCompanyName())
                .createdAt(matchResult.getCreatedAt())
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON", e);
            return null;
        }
    }

    private void persistRecommendations(
            MatchResult matchResult,
            ImprovementPlanResponse improvementPlan
    ) {
        recommendationRepository.deleteByMatchResultId(matchResult.getId());

        if (improvementPlan == null || improvementPlan.getRecommendations() == null) {
            return;
        }

        List<ImprovementRecommendation> entities = new ArrayList<>();

        for (ImprovementPlanResponse.RecommendationResponse item : improvementPlan.getRecommendations()) {
            if (item == null || item.getGapName() == null || item.getGapName().isBlank()) {
                continue;
            }

            ImprovementRecommendation recommendation = ImprovementRecommendation.builder()
                    .matchResult(matchResult)
                    .gapName(item.getGapName())
                    .gapCategory(item.getGapCategory() != null ? item.getGapCategory() : "OTHER")
                    .requiredOrPreferred(parseRequiredOrPreferred(item.getRequiredOrPreferred()))
                    .importanceWeight(clamp(item.getImportanceWeight(), 1, 10, 5))
                    .jobEvidence(item.getJobEvidence())
                    .cvEvidence(item.getCvEvidence())
                    .relatedExistingStrengths(toJson(item.getRelatedExistingStrengths()))
                    .recommendedAction(item.getRecommendedAction() != null && !item.getRecommendedAction().isBlank()
                            ? item.getRecommendedAction()
                            : "Build practical experience with this skill.")
                    .deliverable(item.getDeliverable())
                    .estimatedEffort(item.getEstimatedEffort() != null && !item.getEstimatedEffort().isBlank()
                            ? item.getEstimatedEffort()
                            : "Not specified")
                    .expectedScoreGain(clamp(item.getExpectedScoreGain(), 0, 100, 0))
                    .priorityScore(clamp(item.getPriorityScore(), 0, 100, 50))
                    .priorityLabel(parsePriorityLabel(item.getPriorityLabel()))
                    .confidence(item.getConfidence() != null && !item.getConfidence().isBlank()
                            ? item.getConfidence()
                            : "MEDIUM")
                    .resourcesJson(toJson(item.getResources()))
                    .status(parseRecommendationStatus(item.getStatus()))
                    .completedAt(item.getCompletedAt())
                    .build();

            entities.add(recommendation);
        }

        if (!entities.isEmpty()) {
            recommendationRepository.saveAll(entities);
            log.info("Persisted {} improvement recommendations for match {}",
                    entities.size(), matchResult.getId());
        }
    }

    private ImprovementPlanResponse buildImprovementPlanResponse(MatchResult matchResult) {
        List<ImprovementRecommendation> recommendations =
                recommendationRepository.findByMatchResultIdOrderByPriorityScoreDesc(matchResult.getId());

        List<ImprovementPlanResponse.RecommendationResponse> responses = recommendations.stream()
                .map(this::toRecommendationResponse)
                .toList();

        int high = 0;
        int medium = 0;
        int low = 0;
        int totalGain = 0;

        for (ImprovementRecommendation recommendation : recommendations) {
            totalGain += recommendation.getExpectedScoreGain() != null
                    ? recommendation.getExpectedScoreGain() : 0;
            switch (recommendation.getPriorityLabel()) {
                case VERY_HIGH, HIGH -> high++;
                case MEDIUM -> medium++;
                case LOW -> low++;
            }
        }

        return ImprovementPlanResponse.builder()
                .recommendations(responses)
                .summary(ImprovementPlanResponse.Summary.builder()
                        .totalGaps(recommendations.size())
                        .highPriority(high)
                        .mediumPriority(medium)
                        .lowPriority(low)
                        .expectedScoreGainTotal(totalGain)
                        .build())
                .build();
    }

    private ImprovementPlanResponse.RecommendationResponse toRecommendationResponse(
            ImprovementRecommendation recommendation
    ) {
        return ImprovementPlanResponse.RecommendationResponse.builder()
                .gapId(recommendation.getId())
                .gapName(recommendation.getGapName())
                .gapCategory(recommendation.getGapCategory())
                .requiredOrPreferred(recommendation.getRequiredOrPreferred().name())
                .importanceWeight(recommendation.getImportanceWeight())
                .jobEvidence(recommendation.getJobEvidence())
                .cvEvidence(recommendation.getCvEvidence())
                .relatedExistingStrengths(
                        parseStringList(recommendation.getRelatedExistingStrengths())
                )
                .recommendedAction(recommendation.getRecommendedAction())
                .deliverable(recommendation.getDeliverable())
                .estimatedEffort(recommendation.getEstimatedEffort())
                .expectedScoreGain(recommendation.getExpectedScoreGain())
                .priorityScore(recommendation.getPriorityScore())
                .priorityLabel(recommendation.getPriorityLabel().name())
                .confidence(recommendation.getConfidence())
                .resources(parseResources(recommendation.getResourcesJson()))
                .status(recommendation.getStatus().name())
                .completedAt(recommendation.getCompletedAt())
                .createdAt(recommendation.getCreatedAt())
                .build();
    }

    private List<ImprovementPlanResponse.ResourceResponse> parseResources(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<ImprovementPlanResponse.ResourceResponse>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse recommendation resources", e);
            return new ArrayList<>();
        }
    }

    private ImprovementRecommendation.RequiredOrPreferred parseRequiredOrPreferred(String value) {
        if (value == null || value.isBlank()) {
            return ImprovementRecommendation.RequiredOrPreferred.REQUIRED;
        }
        try {
            return ImprovementRecommendation.RequiredOrPreferred.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImprovementRecommendation.RequiredOrPreferred.REQUIRED;
        }
    }

    private ImprovementRecommendation.PriorityLabel parsePriorityLabel(String value) {
        if (value == null || value.isBlank()) {
            return ImprovementRecommendation.PriorityLabel.MEDIUM;
        }
        try {
            return ImprovementRecommendation.PriorityLabel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImprovementRecommendation.PriorityLabel.MEDIUM;
        }
    }

    private ImprovementRecommendation.RecommendationStatus parseRecommendationStatus(String value) {
        if (value == null || value.isBlank()) {
            return ImprovementRecommendation.RecommendationStatus.PENDING;
        }
        try {
            return ImprovementRecommendation.RecommendationStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ImprovementRecommendation.RecommendationStatus.PENDING;
        }
    }

    private int clamp(Integer value, int min, int max, int defaultValue) {
        int resolved = value != null ? value : defaultValue;
        return Math.max(min, Math.min(max, resolved));
    }


    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            log.warn("Failed to parse related existing strengths", e);
            return new ArrayList<>();
        }
    }
}
