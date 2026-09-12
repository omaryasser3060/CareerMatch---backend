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
public class AdzunaResponseDto {

    @JsonProperty("results")
    private List<AdzunaJobDto> results;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("mean")
    private Double mean;
}