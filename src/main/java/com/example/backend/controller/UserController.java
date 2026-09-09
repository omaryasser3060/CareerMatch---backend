package com.example.backend.controller;

import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.request.UpdateSettingsRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        String userId = getUserId(authentication);
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String userId = getUserId(authentication);
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Object>> getSettings(Authentication authentication) {
        // Return settings (implementation in UserService)
        // For now, return mock response
        return ResponseEntity.ok(ApiResponse.success("Settings retrieved"));
    }

    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<Object>> updateSettings(
            Authentication authentication,
            @Valid @RequestBody UpdateSettingsRequest request
    ) {
        // Update settings (implementation in UserService)
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @RequestParam String currentPassword,
            @RequestParam String newPassword
    ) {
        String userId = getUserId(authentication);
        userService.changePassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(Authentication authentication) {
        String userId = getUserId(authentication);
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    private String getUserId(Authentication authentication) {
        // In production, extract user ID from authentication
        // For now, return a mock ID
        return "user-id";
    }
}