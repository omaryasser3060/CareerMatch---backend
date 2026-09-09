package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHistoryResponse {

    private List<MatchSummaryResponse> results;
    private Integer page;
    private Integer totalPages;
    private Integer totalCount;
    private Integer pageSize;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchSummaryResponse {
        private String matchId;
        private Integer overallMatchScore;
        private String jobTitle;
        private String companyName;
        private String createdAt;
        private Integer requiredSkillsMatch;
    }
}