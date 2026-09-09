package com.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProfileRequest {

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private Preferences preferences;

    @Data
    public static class Preferences {
        private String defaultLocation;
        private Boolean jobAlerts;
        private List<String> preferredRoles;
        private String jobAlertFrequency;
    }
}