package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Match analysis request")
public class MatchAnalysisRequest {

    @NotBlank(message = "CV ID is required")
    @Size(max = 36, message = "CV ID must not exceed 36 characters")
    @Schema(description = "CV ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cvId;

    @NotBlank(message = "Job ID is required")
    @Size(max = 36, message = "Job ID must not exceed 36 characters")
    @Schema(description = "Job ID", example = "550e8400-e29b-41d4-a716-446655440001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jobId;
}