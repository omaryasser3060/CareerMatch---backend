package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CV parsing status response")
public class CVStatusResponse {

    @Schema(description = "CV ID")
    private String cvId;

    @Schema(description = "Whether the CV has been parsed", example = "true")
    private boolean parsed;

    @Schema(description = "Extraction confidence", example = "HIGH")
    private String extractionConfidence;

    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadedAt;

    @Schema(description = "Parsing timestamp")
    private LocalDateTime parsedAt;
}