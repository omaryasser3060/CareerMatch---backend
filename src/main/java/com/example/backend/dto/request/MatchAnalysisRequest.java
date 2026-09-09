package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MatchAnalysisRequest {

    @NotBlank(message = "CV ID is required")
    private String cvId;

    @NotBlank(message = "Job ID is required")
    private String jobId;
}