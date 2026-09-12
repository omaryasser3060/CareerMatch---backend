package com.example.backend.dto.response;

import com.example.backend.model.Job;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Job details response")
public class JobResponse {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Schema(description = "Job ID")
    private String id;

    @Schema(description = "Job title", example = "Senior Backend Developer")
    private String title;

    @Schema(description = "Company name", example = "TechCorp Inc.")
    private String company;

    @Schema(description = "Job location", example = "Remote")
    private String location;

    @Schema(description = "Job description")
    private String description;

    @Schema(description = "Data source", example = "adzuna")
    private String source;

    @Schema(description = "Required skills")
    private List<String> requiredSkills;

    @Schema(description = "Preferred skills")
    private List<String> preferredSkills;

    @Schema(description = "Expected experience in months", example = "24")
    private Integer expectedExperienceMonths;

    @Schema(description = "Job posting date")
    private LocalDateTime postedDate;

    @Schema(description = "Original job URL")
    private String url;

    @Schema(description = "Minimum salary")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum salary")
    private BigDecimal salaryMax;

    @Schema(description = "Salary currency", example = "USD")
    private String currency;

    @Schema(description = "Employment type", example = "FULL_TIME")
    private String employmentType;

    @Schema(description = "Remote type", example = "REMOTE")
    private String remoteType;

    public static JobResponse fromEntity(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .description(job.getDescription())
                .source(job.getSource())
                .requiredSkills(parseSkills(job.getRequiredSkillsJson()))
                .preferredSkills(parseSkills(job.getPreferredSkillsJson()))
                .expectedExperienceMonths(job.getExpectedExperienceMonths())
                .postedDate(job.getPostedDate())
                .url(job.getUrl())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .employmentType(job.getEmploymentType())
                .remoteType(job.getRemoteType())
                .build();
    }

    private static List<String> parseSkills(String skillsJson) {
        if (skillsJson == null || skillsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(skillsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}