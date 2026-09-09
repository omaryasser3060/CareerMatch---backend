package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVProfileResponse {

    private String cvId;
    private List<String> skills;
    private Integer experienceMonths;
    private List<ProjectResponse> projects;
    private EducationResponse education;
    private String extractionConfidence;
    private String experienceSummary;
    private List<String> certifications;
    private List<String> languages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectResponse {
        private String title;
        private List<String> skillsEvidence;
        private String description;
        private String startDate;
        private String endDate;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationResponse {
        private String degree;
        private String level;
        private String field;
        private String institution;
        private Integer graduationYear;
        private Double gpa;
    }
}