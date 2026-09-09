package com.example.backend.controller;

import com.example.backend.dto.request.MatchAnalysisRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.dto.response.MatchAnalysisResponse;
import com.example.backend.dto.response.MatchHistoryResponse;
import com.example.backend.service.MatchService;
import com.example.backend.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final RecommendationService recommendationService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<MatchAnalysisResponse>> analyzeMatch(
            Authentication authentication,
            @Valid @RequestBody MatchAnalysisRequest request
    ) {
        String userId = getUserId(authentication);
        MatchAnalysisResponse response = matchService.analyzeMatch(userId, request.getCvId(), request.getJobId());
        return ResponseEntity.ok(ApiResponse.success("Analysis completed", response));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchAnalysisResponse>> getMatchResult(
            Authentication authentication,
            @PathVariable String matchId
    ) {
        String userId = getUserId(authentication);
        MatchAnalysisResponse response = matchService.getMatchResult(userId, matchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<MatchHistoryResponse>> getMatchHistory(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo
    ) {
        String userId = getUserId(authentication);
        MatchHistoryResponse response = matchService.getMatchHistory(
                userId, page, pageSize, minScore, maxScore, jobTitle, dateFrom, dateTo
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{matchId}/plan")
    public ResponseEntity<ApiResponse<ImprovementPlanResponse>> getImprovementPlan(
            Authentication authentication,
            @PathVariable String matchId
    ) {
        String userId = getUserId(authentication);
        ImprovementPlanResponse response = recommendationService.getImprovementPlan(userId, matchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/recommendation/{gapId}")
    public ResponseEntity<ApiResponse<Object>> updateRecommendationStatus(
            Authentication authentication,
            @PathVariable String gapId,
            @RequestParam String status
    ) {
        String userId = getUserId(authentication);
        recommendationService.updateRecommendationStatus(userId, gapId, status);
        return ResponseEntity.ok(ApiResponse.success("Recommendation status updated", null));
    }

    private String getUserId(Authentication authentication) {
        // In production, extract user ID from authentication
        return "user-id";
    }
}