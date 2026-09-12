package com.example.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobRequirementsDto {

    @Builder.Default
    @JsonProperty("required_skills")
    private List<String> requiredSkills = new ArrayList<>();

    @Builder.Default
    @JsonProperty("preferred_skills")
    private List<String> preferredSkills = new ArrayList<>();

    @JsonProperty("experience_level")
    private String experienceLevel;

    @JsonProperty("experience_months")
    private Integer experienceMonths;

    @Builder.Default
    @JsonProperty("responsibilities")
    private List<String> responsibilities = new ArrayList<>();

    @Builder.Default
    @JsonProperty("education_requirements")
    private List<String> educationRequirements = new ArrayList<>();

    @Builder.Default
    @JsonProperty("certification_requirements")
    private List<String> certificationRequirements = new ArrayList<>();

    @Builder.Default
    @JsonProperty("language_requirements")
    private List<String> languageRequirements = new ArrayList<>();

    @Builder.Default
    @JsonProperty("other_requirements")
    private List<String> otherRequirements = new ArrayList<>();

    @JsonProperty("confidence")
    private String confidence;
}