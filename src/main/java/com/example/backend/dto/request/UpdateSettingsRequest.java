package com.example.backend.dto.request;

import lombok.Data;

@Data
public class UpdateSettingsRequest {

    private Boolean emailNotifications;
    private Boolean darkMode;
    private String language;
    private String theme;
    private Boolean jobAlerts;
    private String jobAlertFrequency;
    private Boolean marketingEmails;
    private Boolean shareAnalytics;
}