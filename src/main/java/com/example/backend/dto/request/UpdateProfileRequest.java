package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user profile request")
public class UpdateProfileRequest {

    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z\\s\\-'.]+$", message = "Name contains invalid characters")
    @Schema(description = "User full name", example = "John Doe")
    private String name;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Valid
    @Schema(description = "User preferences")
    private Preferences preferences;

    // ============================================================
    // Nested DTO
    // ============================================================

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User preferences")
    public static class Preferences {

        @Size(max = 255, message = "Default location must not exceed 255 characters")
        @Schema(description = "Default job search location", example = "Cairo, Egypt")
        private String defaultLocation;

        @Schema(description = "Whether to receive job alerts", example = "true")
        private Boolean jobAlerts;

        @Size(max = 10, message = "Maximum 10 preferred roles")
        @Schema(description = "List of preferred job roles")
        private List<String> preferredRoles;

        @Pattern(
                regexp = "^(DAILY|WEEKLY|MONTHLY|NEVER)$",
                message = "Job alert frequency must be DAILY, WEEKLY, MONTHLY, or NEVER"
        )
        @Schema(description = "Job alert frequency", example = "DAILY")
        private String jobAlertFrequency;
    }
}