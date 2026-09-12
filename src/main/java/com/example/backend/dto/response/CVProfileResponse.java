package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parsed CV profile response")
public class CVProfileResponse {

    @Schema(description = "CV ID")
    private String cvId;

    @Schema(description = "List of extracted skills")
    private List<String> skills;

    @Schema(description = "Total experience in months", example = "24")
    @JsonProperty("experience_months")
    private Integer experienceMonths;

    @Schema(description = "Experience summary")
    @JsonProperty("experience_summary")
    private String experienceSummary;

    @Schema(description = "List of projects")
    private List<ProjectResponse> projects;

    @Schema(description = "Education information")
    private EducationResponse education;

    @Schema(description = "List of certifications")
    private List<String> certifications;

    @Schema(description = "List of languages")
    private List<String> languages;

    @Schema(description = "Extraction confidence", example = "HIGH")
    private String extractionConfidence;

    @Schema(description = "List of past job titles")
    @JsonProperty("past_titles")
    private List<String> pastTitles;

    // ============================================================
    // Nested DTOs
    // ============================================================

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Project information")
    public static class ProjectResponse {

        @Schema(description = "Project title")
        private String title;

        @Schema(description = "Project description")
        private String description;

        @Schema(description = "Skills used in project")
        @JsonProperty("skills_evidence")
        private List<String> skillsEvidence;

        @Schema(description = "Start date")
        @JsonProperty("start_date")
        private String startDate;

        @Schema(description = "End date")
        @JsonProperty("end_date")
        private String endDate;

        @Schema(description = "Project URL")
        private String url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Education information")
    public static class EducationResponse {

        @Schema(description = "Degree name")
        private String degree;

        @Schema(description = "Education level", example = "BACHELOR")
        private String level;

        @Schema(description = "Field of study")
        private String field;

        @Schema(description = "Institution name")
        private String institution;

        @Schema(description = "Graduation year")
        @JsonProperty("graduation_year")
        private Integer graduationYear;

        @Schema(description = "GPA")
        private Double gpa;
    }
}