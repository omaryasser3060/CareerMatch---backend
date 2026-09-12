package com.example.backend.service;

import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.request.UpdateSettingsRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.dto.response.UserSettingsResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.exception.ValidationException;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // ============================================================
    // Get Profile
    // ============================================================

    @Transactional(readOnly = true)
    public UserResponse getProfile(String userId) {
        User user = findUserById(userId);
        return UserResponse.fromEntity(user);
    }

    // ============================================================
    // Update Profile
    // ============================================================

    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                    throw new ValidationException("Email already taken");
                }
                user.setEmail(newEmail);
                user.setEmailVerified(false); // Require re-verification
            }
        }

        if (request.getPreferences() != null) {
            try {
                user.setPreferences(objectMapper.writeValueAsString(request.getPreferences()));
            } catch (Exception e) {
                log.error("Failed to serialize preferences", e);
                throw new ValidationException("Invalid preferences format");
            }
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return UserResponse.fromEntity(user);
    }

    // ============================================================
    // Get Settings
    // ============================================================

    @Transactional(readOnly = true)
    public UserSettingsResponse getSettings(String userId) {
        User user = findUserById(userId);
        return buildSettingsResponse(user);
    }

    // ============================================================
    // Update Settings
    // ============================================================

    @Transactional
    public UserSettingsResponse updateSettings(String userId, UpdateSettingsRequest request) {
        User user = findUserById(userId);

        // Parse existing preferences
        UserSettingsResponse settings = buildSettingsResponse(user);

        // Update fields
        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getTheme() != null) {
            settings.setTheme(request.getTheme());
        }
        if (request.getJobAlerts() != null) {
            settings.setJobAlerts(request.getJobAlerts());
        }
        if (request.getJobAlertFrequency() != null) {
            settings.setJobAlertFrequency(request.getJobAlertFrequency());
        }
        if (request.getMarketingEmails() != null) {
            settings.setMarketingEmails(request.getMarketingEmails());
        }
        if (request.getShareAnalytics() != null) {
            settings.setShareAnalytics(request.getShareAnalytics());
        }

        // Save as JSON
        try {
            user.setPreferences(objectMapper.writeValueAsString(settings));
        } catch (Exception e) {
            log.error("Failed to serialize settings", e);
            throw new ValidationException("Invalid settings format");
        }

        userRepository.save(user);
        log.info("Settings updated for user: {}", userId);
        return settings;
    }

    // ============================================================
    // Change Password
    // ============================================================

    @Transactional
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    // ============================================================
    // Delete Account
    // ============================================================

    @Transactional
    public void deleteAccount(String userId) {
        User user = findUserById(userId);
        userRepository.delete(user); // Soft delete via @SQLDelete
        log.info("Account deleted (soft) for user: {}", userId);
    }

    // ============================================================
    // Find Methods
    // ============================================================

    @Transactional(readOnly = true)
    public User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private UserSettingsResponse buildSettingsResponse(User user) {
        UserSettingsResponse settings = UserSettingsResponse.builder()
                .userId(user.getId())
                .preferences(user.getPreferences())
                .emailNotifications(true)
                .pushNotifications(true)
                .language("en")
                .theme("SYSTEM")
                .jobAlerts(true)
                .jobAlertFrequency("DAILY")
                .marketingEmails(false)
                .shareAnalytics(true)
                .build();

        // Parse from preferences JSON if exists
        if (user.getPreferences() != null && !user.getPreferences().isBlank()) {
            try {
                settings = objectMapper.readValue(user.getPreferences(), UserSettingsResponse.class);
                settings.setUserId(user.getId());
                settings.setPreferences(user.getPreferences());
            } catch (Exception e) {
                log.warn("Failed to parse user preferences for user: {}", user.getId());
            }
        }

        return settings;
    }
}