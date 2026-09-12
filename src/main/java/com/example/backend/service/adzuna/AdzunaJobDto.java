package com.example.backend.service.adzuna;

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
public class AdzunaJobDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("company")
    private CompanyDto company;

    @JsonProperty("location")
    private LocationDto location;

    @JsonProperty("salary_min")
    private Double salaryMin;

    @JsonProperty("salary_max")
    private Double salaryMax;

    @JsonProperty("created")
    private String created;

    @JsonProperty("redirect_url")
    private String redirectUrl;

    @JsonProperty("contract_time")
    private String contractTime;

    @JsonProperty("contract_type")
    private String contractType;

    @JsonProperty("category")
    private CategoryDto category;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyDto {

        @JsonProperty("display_name")
        private String displayName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationDto {

        @JsonProperty("display_name")
        private String displayName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CategoryDto {

        @JsonProperty("label")
        private String label;

        @JsonProperty("tag")
        private String tag;
    }
}