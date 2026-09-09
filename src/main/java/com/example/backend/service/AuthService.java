package com.example.backend.service;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.exception.ValidationException;
import com.example.backend.model.RefreshToken;
import com.example.backend.model.User;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        // Check if user exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .onboardingCompleted(false)
                .emailVerified(false)
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String token = jwtService.generateToken(createUserDetails(user));
        String refreshToken = jwtService.generateRefreshToken(createUserDetails(user));

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .user(UserResponse.fromEntity(user))
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Generate tokens
        String token = jwtService.generateToken(createUserDetails(user));
        String refreshToken = jwtService.generateRefreshToken(createUserDetails(user));

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .user(UserResponse.fromEntity(user))
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.isExpired()) {
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = token.getUser();
        String newToken = jwtService.generateToken(createUserDetails(user));
        String newRefreshToken = jwtService.generateRefreshToken(createUserDetails(user));

        // Revoke old token and save new one
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        saveRefreshToken(user, newRefreshToken);

        return AuthResponse.builder()
                .user(UserResponse.fromEntity(user))
                .token(newToken)
                .refreshToken(newRefreshToken)
                .expiresIn(86400000L)
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        // Implementation for email verification
        // This would typically use a separate verification token
        // For now, we'll just mark the user as verified
        // This is a placeholder
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private org.springframework.security.core.userdetails.User createUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                java.util.Collections.emptyList()
        );
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        // Delete existing refresh token if exists
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(refreshToken);
    }
}