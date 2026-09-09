package com.example.backend.service;

import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.dto.response.MatchAnalysisResponse;
import com.example.backend.dto.response.MatchHistoryResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.CV;
import com.example.backend.model.Job;
import com.example.backend.model.MatchResult;
import com.example.backend.model.User;
import com.example.backend.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchResultRepository matchResultRepository;
    private final UserService userService;
    private final CVService cvService;
    private final JobService jobService;

    @Transactional
    public MatchAnalysisResponse analyzeMatch(String userId, String cvId, String jobId) {
        User user = userService.findUserById(userId);
        CV cv = cvService.getCV(userId, cvId);
        Job job = jobService.getJobEntity(jobId);

        if (!cv.isParsed()) {
            throw new RuntimeException("CV is not parsed yet. Please wait for processing.");
        }

        // TODO: Call AI Service for match analysis
        // For now, return mock data
        MatchAnalysisResponse response = createMockMatchAnalysis();

        // Save match result
        MatchResult matchResult = MatchResult.builder()
                .user(user)
                .cv(cv)
                .job(job)
                .overallMatchScore(response.getOverallMatchScore())
                .scoreBreakdownJson("{}")
                .matchedSkillsJson("{}")
                .missingSkillsJson("{}")
                .strengthsJson("{}")
                .improvementPlanJson("{}")
                .evidenceJson("{}")
                .extractionConfidence(response.getExtractionConfidence())
                .humanReviewFlag(response.getHumanReviewFlag())
                .jobTitle(job.getTitle())
                .companyName(job.getCompany())
                .build();

        matchResult = matchResultRepository.save(matchResult);
        response.setMatchId(matchResult.getId());

        // Save improvement plan
        // TODO: Save improvement recommendations

        return response;
    }

    public MatchAnalysisResponse getMatchResult(String userId, String matchId) {
        MatchResult matchResult = matchResultRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Match result", "id", matchId));

        // Parse from stored JSON
        // For now, return mock data
        MatchAnalysisResponse response = createMockMatchAnalysis();
        response.setMatchId(matchResult.getId());
        return response;
    }

    public MatchHistoryResponse getMatchHistory(
            String userId,
            Integer page,
            Integer pageSize,
            Integer minScore,
            Integer maxScore,
            String jobTitle,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        int currentPage = page != null ? page : 1;
        int size = pageSize != null ? pageSize : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, size);

        Page<MatchResult> matchPage = matchResultRepository.filterMatches(
                userId,
                minScore,
                maxScore,
                jobTitle,
                dateFrom,
                dateTo,
                pageable
        );

        List<MatchHistoryResponse.MatchSummaryResponse> summaries = matchPage.getContent()
                .stream()
                .map(m -> MatchHistoryResponse.MatchSummaryResponse.builder()
                        .matchId(m.getId())
                        .overallMatchScore(m.getOverallMatchScore())
                        .jobTitle(m.getJobTitle())
                        .companyName(m.getCompanyName())
                        .createdAt(m.getCreatedAt().toString())
                        .requiredSkillsMatch(0) // Calculate from breakdown
                        .build()
                )
                .collect(Collectors.toList());

        return MatchHistoryResponse.builder()
                .results(summaries)
                .page(currentPage)
                .totalPages(matchPage.getTotalPages())
                .totalCount((int) matchPage.getTotalElements())
                .pageSize(size)
                .build();
    }

    private MatchAnalysisResponse createMockMatchAnalysis() {
        // Create mock response for testing
        MatchAnalysisResponse response = new MatchAnalysisResponse();
        response.setOverallMatchScore(75);
        response.setExtractionConfidence("high");
        response.setHumanReviewFlag(false);
        return response;
    }
}