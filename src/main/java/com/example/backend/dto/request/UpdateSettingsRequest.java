package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update user settings request")
public class UpdateSettingsRequest {

    @Schema(description = "Whether to receive email notifications", example = "true")
    private Boolean emailNotifications;

    @Schema(description = "Whether to use dark mode", example = "false")
    private Boolean darkMode;

    @Pattern(
            regexp = "^(en|ar|fr|es|de)$",
            message = "Language must be one of: en, ar, fr, es, de"
    )
    @Schema(description = "Preferred language", example = "en")
    private String language;

    @Pattern(
            regexp = "^(LIGHT|DARK|SYSTEM)$",
            message = "Theme must be LIGHT, DARK, or SYSTEM"
    )
    @Schema(description = "UI theme", example = "SYSTEM")
    private String theme;

    @Schema(description = "Whether to receive job alerts", example = "true")
    private Boolean jobAlerts;

    @Pattern(
            regexp = "^(DAILY|WEEKLY|MONTHLY|NEVER)$",
            message = "Job alert frequency must be DAILY, WEEKLY, MONTHLY, or NEVER"
    )
    @Schema(description = "Job alert frequency", example = "DAILY")
    private String jobAlertFrequency;

    @Schema(description = "Whether to receive marketing emails", example = "false")
    private Boolean marketingEmails;

    @Schema(description = "Whether to share analytics data", example = "true")
    private Boolean shareAnalytics;
}