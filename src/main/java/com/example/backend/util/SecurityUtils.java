package com.example.backend.util;

import com.example.backend.exception.UnauthorizedException;
import com.example.backend.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility class for extracting security-related information from the current context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the current authenticated user's ID.
     */
    public static String getCurrentUserId() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getId)
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
    }

    /**
     * Get the current authenticated user's ID, if present.
     */
    public static Optional<String> getCurrentUserIdOptional() {
        return getCurrentUserDetails().map(CustomUserDetails::getId);
    }

    /**
     * Get the current authenticated user's email.
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserDetails()
                .map(CustomUserDetails::getEmail)
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
    }

    /**
     * Get the current authenticated user's role.
     */
    public static String getCurrentUserRole() {
        return getCurrentUserDetails()
                .map(details -> details.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .orElse("USER"))
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));
    }

    /**
     * Get the current authenticated user's details.
     */
    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return Optional.of(customUserDetails);
        }

        return Optional.empty();
    }

    /**
     * Check if the current user has a specific role.
     */
    public static boolean hasRole(String role) {
        return getCurrentUserDetails()
                .map(details -> details.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + role)))
                .orElse(false);
    }

    /**
     * Check if the current user is an admin.
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user is authenticated.
     */
    public static boolean isAuthenticated() {
        return getCurrentUserDetails().isPresent();
    }
}