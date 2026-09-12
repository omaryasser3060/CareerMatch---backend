package com.example.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidateProfileDto {

    @JsonProperty("skills")
    private List<String> skills;

    @JsonProperty("experience_summary")
    private String experienceSummary;

    @JsonProperty("years_of_experience")
    private Integer yearsOfExperience;

    @JsonProperty("experience_months")
    private Integer experienceMonths;

    @JsonProperty("past_titles")
    private List<String> pastTitles;

    @JsonProperty("projects")
    private List<ProjectDto> projects;

    @JsonProperty("education")
    private EducationDto education;

    @JsonProperty("certifications")
    private List<String> certifications;

    @JsonProperty("languages")
    private List<String> languages;

    @JsonProperty("confidence")
    private String confidence;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProjectDto {
        private String title;
        private String description;
        @JsonProperty("skills_evidence")
        private List<String> skillsEvidence;
        @JsonProperty("start_date")
        private String startDate;
        @JsonProperty("end_date")
        private String endDate;
        private String url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EducationDto {
        private String degree;
        private String level;
        private String field;
        private String institution;
        @JsonProperty("graduation_year")
        private Integer graduationYear;
        private Double gpa;
    }
}