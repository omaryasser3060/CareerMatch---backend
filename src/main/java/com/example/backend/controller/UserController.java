package com.example.backend.controller;

import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.request.UpdateSettingsRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.dto.response.UserSettingsResponse;
import com.example.backend.service.UserService;
import com.example.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "User Management", description = "User profile and settings endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Returns the current user's profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Updates the current user's profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Update profile request for user: {}", userId);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/settings")
    @Operation(summary = "Get user settings", description = "Returns the current user's settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getSettings() {
        String userId = SecurityUtils.getCurrentUserId();
        UserSettingsResponse response = userService.getSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/settings")
    @Operation(summary = "Update user settings", description = "Updates the current user's settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Update settings request for user: {}", userId);
        UserSettingsResponse response = userService.updateSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", response));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the current user's password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Current password incorrect")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Change password request for user: {}", userId);
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/account")
    @Operation(summary = "Delete account", description = "Deletes the current user's account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        String userId = SecurityUtils.getCurrentUserId();
        log.warn("Delete account request for user: {}", userId);
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}