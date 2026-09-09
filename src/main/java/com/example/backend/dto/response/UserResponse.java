package com.example.backend.dto.response;

import com.example.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String name;
    private Boolean onboardingCompleted;
    private LocalDateTime createdAt;
    private Boolean emailVerified;
    private String role;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .onboardingCompleted(user.isOnboardingCompleted())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.isEmailVerified())
                .role(user.getRole().name())
                .build();
    }
}