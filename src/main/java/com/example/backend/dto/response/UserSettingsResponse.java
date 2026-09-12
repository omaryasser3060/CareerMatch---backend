package com.example.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User settings response")
public class UserSettingsResponse {

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "User preferences JSON")
    private String preferences;

    @Schema(description = "Whether email notifications are enabled", example = "true")
    private boolean emailNotifications;

    @Schema(description = "Whether push notifications are enabled", example = "true")
    private boolean pushNotifications;

    @Schema(description = "Preferred language", example = "en")
    private String language;

    @Schema(description = "UI theme", example = "SYSTEM")
    private String theme;

    @Schema(description = "Whether job alerts are enabled", example = "true")
    private boolean jobAlerts;

    @Schema(description = "Job alert frequency", example = "DAILY")
    private String jobAlertFrequency;

    @Schema(description = "Whether marketing emails are enabled", example = "false")
    private boolean marketingEmails;

    @Schema(description = "Whether analytics sharing is enabled", example = "true")
    private boolean shareAnalytics;
}