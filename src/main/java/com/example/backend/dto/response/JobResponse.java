package com.example.backend.dto.response;

import com.example.backend.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private String id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String source;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private Integer expectedExperienceMonths;
    private LocalDateTime postedDate;
    private String url;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String employmentType;
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
        return List.of();
    }
}