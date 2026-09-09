package com.example.backend.service;

import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.ImprovementRecommendation;
import com.example.backend.model.MatchResult;
import com.example.backend.repository.ImprovementRecommendationRepository;
import com.example.backend.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ImprovementRecommendationRepository recommendationRepository;
    private final MatchResultRepository matchResultRepository;

    public ImprovementPlanResponse getImprovementPlan(String userId, String matchId) {
        MatchResult matchResult = matchResultRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        List<ImprovementRecommendation> recommendations = recommendationRepository.findByMatchResultIdOrderByPriorityScoreDesc(matchId);

        if (recommendations.isEmpty()) {
            return createEmptyPlan();
        }

        return convertToResponse(recommendations);
    }

    @Transactional
    public ImprovementPlanResponse.RecommendationResponse updateRecommendationStatus(
            String userId,
            String gapId,
            String status
    ) {
        ImprovementRecommendation recommendation = recommendationRepository.findById(gapId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation", "id", gapId));

        // Verify ownership
        String matchUserId = recommendation.getMatchResult().getUser().getId();
        if (!matchUserId.equals(userId)) {
            throw new RuntimeException("Unauthorized to update this recommendation");
        }

        ImprovementRecommendation.RecommendationStatus newStatus =
                ImprovementRecommendation.RecommendationStatus.valueOf(status.toUpperCase());

        recommendation.setStatus(newStatus);
        if (newStatus == ImprovementRecommendation.RecommendationStatus.COMPLETED) {
            recommendation.setCompletedAt(LocalDateTime.now());
        }
        recommendation.setUpdatedAt(LocalDateTime.now());

        recommendation = recommendationRepository.save(recommendation);

        return convertToResponse(recommendation);
    }

    @Transactional
    public void saveRecommendations(String matchId, List<ImprovementRecommendation> recommendations) {
        MatchResult matchResult = matchResultRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        // Delete existing recommendations
        recommendationRepository.deleteByMatchResultId(matchId);

        for (ImprovementRecommendation rec : recommendations) {
            rec.setMatchResult(matchResult);
            rec.setCreatedAt(LocalDateTime.now());
            rec.setUpdatedAt(LocalDateTime.now());
            recommendationRepository.save(rec);
        }
    }

    private ImprovementPlanResponse createEmptyPlan() {
        ImprovementPlanResponse response = new ImprovementPlanResponse();
        response.setRecommendations(new ArrayList<>());
        response.setSummary(
                ImprovementPlanResponse.Summary.builder()
                        .totalGaps(0)
                        .highPriority(0)
                        .mediumPriority(0)
                        .lowPriority(0)
                        .expectedScoreGainTotal(0)
                        .build()
        );
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    private ImprovementPlanResponse convertToResponse(List<ImprovementRecommendation> recommendations) {
        List<ImprovementPlanResponse.RecommendationResponse> recResponses = recommendations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        int highCount = 0, mediumCount = 0, lowCount = 0;
        int totalGain = 0;

        for (ImprovementRecommendation rec : recommendations) {
            totalGain += rec.getExpectedScoreGain();
            switch (rec.getPriorityLabel()) {
                case VERY_HIGH:
                case HIGH:
                    highCount++;
                    break;
                case MEDIUM:
                    mediumCount++;
                    break;
                case LOW:
                    lowCount++;
                    break;
            }
        }

        ImprovementPlanResponse.Summary summary = ImprovementPlanResponse.Summary.builder()
                .totalGaps(recommendations.size())
                .highPriority(highCount)
                .mediumPriority(mediumCount)
                .lowPriority(lowCount)
                .expectedScoreGainTotal(totalGain)
                .build();

        return ImprovementPlanResponse.builder()
                .recommendations(recResponses)
                .summary(summary)
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
                .relatedExistingStrengths(List.of()) // Parse from JSON
                .recommendedAction(rec.getRecommendedAction())
                .deliverable(rec.getDeliverable())
                .estimatedEffort(rec.getEstimatedEffort())
                .expectedScoreGain(rec.getExpectedScoreGain())
                .priorityScore(rec.getPriorityScore())
                .priorityLabel(rec.getPriorityLabel().name())
                .confidence(rec.getConfidence())
                .resources(List.of()) // Parse from JSON
                .status(rec.getStatus().name())
                .build();
    }
}