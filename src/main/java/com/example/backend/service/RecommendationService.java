package com.example.backend.service;

import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.model.ImprovementRecommendation;
import com.example.backend.model.MatchResult;
import com.example.backend.repository.ImprovementRecommendationRepository;
import com.example.backend.repository.MatchResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final ImprovementRecommendationRepository recommendationRepository;
    private final MatchResultRepository matchResultRepository;
    private final ObjectMapper objectMapper;

    // ============================================================
    // Get Improvement Plan
    // ============================================================

    @Transactional(readOnly = true)
    public ImprovementPlanResponse getImprovementPlan(String userId, String matchId) {
        MatchResult matchResult = matchResultRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        List<ImprovementRecommendation> recommendations =
                recommendationRepository.findByMatchResultIdOrderByPriorityScoreDesc(matchId);

        if (recommendations.isEmpty()) {
            return createEmptyPlan();
        }

        return convertToResponse(recommendations);
    }

    // ============================================================
    // Update Status
    // ============================================================

    @Transactional
    public void updateRecommendationStatus(
            String userId,
            String gapId,
            ImprovementRecommendation.RecommendationStatus status
    ) {
        ImprovementRecommendation recommendation = recommendationRepository.findById(gapId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", gapId));

        // Verify ownership
        String matchUserId = recommendation.getMatchResult().getUser().getId();
        if (!matchUserId.equals(userId)) {
            throw new UnauthorizedException("Unauthorized to update this recommendation");
        }

        recommendation.setStatus(status);
        if (status == ImprovementRecommendation.RecommendationStatus.COMPLETED) {
            recommendation.setCompletedAt(LocalDateTime.now());
        }

        recommendationRepository.save(recommendation);
        log.info("Recommendation status updated: {} -> {}", gapId, status);
    }

    // ============================================================
    // Save Recommendations
    // ============================================================

    @Transactional
    public void saveRecommendations(String matchId, List<ImprovementRecommendation> recommendations) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        recommendationRepository.deleteByMatchResultId(matchId);

        for (ImprovementRecommendation rec : recommendations) {
            rec.setMatchResult(matchResult);
            recommendationRepository.save(rec);
        }

        log.info("Saved {} recommendations for match: {}", recommendations.size(), matchId);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private ImprovementPlanResponse createEmptyPlan() {
        return ImprovementPlanResponse.builder()
                .recommendations(new ArrayList<>())
                .summary(ImprovementPlanResponse.Summary.builder()
                        .totalGaps(0)
                        .highPriority(0)
                        .mediumPriority(0)
                        .lowPriority(0)
                        .expectedScoreGainTotal(0)
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ImprovementPlanResponse convertToResponse(List<ImprovementRecommendation> recommendations) {
        List<ImprovementPlanResponse.RecommendationResponse> recResponses = recommendations.stream()
                .map(this::convertToResponse)
                .toList();

        int highCount = 0, mediumCount = 0, lowCount = 0, totalGain = 0;

        for (ImprovementRecommendation rec : recommendations) {
            totalGain += rec.getExpectedScoreGain();
            switch (rec.getPriorityLabel()) {
                case VERY_HIGH, HIGH -> highCount++;
                case MEDIUM -> mediumCount++;
                case LOW -> lowCount++;
            }
        }

        return ImprovementPlanResponse.builder()
                .recommendations(recResponses)
                .summary(ImprovementPlanResponse.Summary.builder()
                        .totalGaps(recommendations.size())
                        .highPriority(highCount)
                        .mediumPriority(mediumCount)
                        .lowPriority(lowCount)
                        .expectedScoreGainTotal(totalGain)
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ImprovementPlanResponse.RecommendationResponse convertToResponse(ImprovementRecommendation rec) {
        return ImprovementPlanResponse.RecommendationResponse.builder()
                .gapId(rec.getId())
                .gapName(rec.getGapName())
                .gapCategory(rec.getGapCategory())
                .requiredOrPreferred(rec.getRequiredOrPreferred().name())
                .importanceWeight(rec.getImportanceWeight())
                .jobEvidence(rec.getJobEvidence())
                .cvEvidence(rec.getCvEvidence())
                .relatedExistingStrengths(parseStringList(rec.getRelatedExistingStrengths()))
                .recommendedAction(rec.getRecommendedAction())
                .deliverable(rec.getDeliverable())
                .estimatedEffort(rec.getEstimatedEffort())
                .expectedScoreGain(rec.getExpectedScoreGain())
                .priorityScore(rec.getPriorityScore())
                .priorityLabel(rec.getPriorityLabel().name())
                .confidence(rec.getConfidence())
                .resources(parseResources(rec.getResourcesJson()))
                .status(rec.getStatus().name())
                .completedAt(rec.getCompletedAt())
                .createdAt(rec.getCreatedAt())
                .build();
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.warn("Failed to parse related strengths JSON", e);
            return new ArrayList<>();
        }
    }

    private List<ImprovementPlanResponse.ResourceResponse> parseResources(String json) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            ImprovementPlanResponse.ResourceResponse.class
                    ));
        } catch (Exception e) {
            log.warn("Failed to parse recommendation resources", e);
            return new ArrayList<>();
        }
    }

}
