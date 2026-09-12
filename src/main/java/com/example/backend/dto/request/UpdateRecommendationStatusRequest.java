package com.example.backend.dto.request;

import com.example.backend.model.ImprovementRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update recommendation status request")
public class UpdateRecommendationStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(
            description = "New recommendation status",
            example = "IN_PROGRESS",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "SKIPPED"}
    )
    private ImprovementRecommendation.RecommendationStatus status;
}