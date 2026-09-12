package com.example.backend.controller;

import com.example.backend.dto.request.MatchAnalysisRequest;
import com.example.backend.dto.request.UpdateRecommendationStatusRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ImprovementPlanResponse;
import com.example.backend.dto.response.MatchAnalysisResponse;
import com.example.backend.dto.response.MatchHistoryResponse;
import com.example.backend.service.MatchService;
import com.example.backend.service.RecommendationService;
import com.example.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Match Analysis", description = "AI-powered match analysis and recommendations")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final MatchService matchService;
    private final RecommendationService recommendationService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze match", description = "Analyzes a CV against a job and returns match score, gaps, and recommendations")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Analysis completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CV or Job not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MatchAnalysisResponse>> analyzeMatch(
            @Valid @RequestBody MatchAnalysisRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Match analysis request: cvId={}, jobId={}, userId={}",
                request.getCvId(), request.getJobId(), userId);

        MatchAnalysisResponse response = matchService.analyzeMatch(
                userId, request.getCvId(), request.getJobId()
        );

        log.info("Match analysis completed: matchId={}, score={}",
                response.getMatchId(), response.getOverallMatchScore());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Analysis completed", response));
    }

    @GetMapping("/{matchId}")
    @Operation(summary = "Get match result", description = "Returns a previously computed match result")
    public ResponseEntity<ApiResponse<MatchAnalysisResponse>> getMatchResult(
            @PathVariable String matchId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        MatchAnalysisResponse response = matchService.getMatchResult(userId, matchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get match history", description = "Returns a paginated list of the user's past match analyses")
    public ResponseEntity<ApiResponse<MatchHistoryResponse>> getMatchHistory(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String companyName
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        MatchHistoryResponse response = matchService.getMatchHistory(
                userId, pageable, minScore, maxScore, jobTitle, companyName
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{matchId}/plan")
    @Operation(summary = "Get improvement plan", description = "Returns the improvement plan for a match result")
    public ResponseEntity<ApiResponse<ImprovementPlanResponse>> getImprovementPlan(
            @PathVariable String matchId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        ImprovementPlanResponse response = recommendationService.getImprovementPlan(userId, matchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/recommendation/{gapId}")
    @Operation(summary = "Update recommendation status", description = "Updates the status of an improvement recommendation")
    public ResponseEntity<ApiResponse<Void>> updateRecommendationStatus(
            @PathVariable String gapId,
            @Valid @RequestBody UpdateRecommendationStatusRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Update recommendation status: gapId={}, status={}, userId={}",
                gapId, request.getStatus(), userId);

        recommendationService.updateRecommendationStatus(userId, gapId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Recommendation status updated", null));
    }

    @DeleteMapping("/{matchId}")
    @Operation(summary = "Delete match result", description = "Deletes a match result and its recommendations")
    public ResponseEntity<ApiResponse<Void>> deleteMatchResult(
            @PathVariable String matchId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Delete match result: matchId={}, userId={}", matchId, userId);
        matchService.deleteMatchResult(userId, matchId);
        return ResponseEntity.ok(ApiResponse.success("Match result deleted", null));
    }
}