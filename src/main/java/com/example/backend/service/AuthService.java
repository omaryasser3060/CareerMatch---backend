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
import com.example.backend.security.CustomUserDetails;
import com.example.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    // ============================================================
    // Register
    // ============================================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Validate passwords match (also validated by @AssertTrue)
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists: {}", request.getEmail());
            throw new ValidationException("Email already registered");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .name(request.getName().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .onboardingCompleted(false)
                .emailVerified(false)
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully: {}", user.getId());

        // Generate verification token
        String verificationToken = generateVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        // Generate tokens
        return buildAuthResponse(user);
    }

    // ============================================================
    // Login
    // ============================================================

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
            log.debug("Authentication successful for: {}", request.getEmail());
        } catch (BadCredentialsException e) {
            log.warn("Login failed: invalid credentials for: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        log.info("User logged in successfully: {}", user.getId());
        return buildAuthResponse(user);
    }

    // ============================================================
    // Refresh Token
    // ============================================================

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Token refresh attempt");

        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.isExpired()) {
            log.warn("Refresh token expired for user: {}", token.getUser().getId());
            token.revoke();
            refreshTokenRepository.save(token);
            throw new UnauthorizedException("Refresh token expired");
        }

        User user = token.getUser();

        // Revoke old token (token rotation)
        token.revoke();
        refreshTokenRepository.save(token);

        log.info("Token refreshed for user: {}", user.getId());
        return buildAuthResponse(user);
    }

    // ============================================================
    // Logout
    // ============================================================

    @Transactional
    public void logout(String userId) {
        log.info("Logout attempt for user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User logged out successfully: {}", userId);
    }

    // ============================================================
    // Email Verification
    // ============================================================

    @Transactional
    public void verifyEmail(String token) {
        log.info("Email verification attempt");

        // In production, parse JWT or lookup verification token
        // For now, use a simple approach
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(generateVerificationToken(u)))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Invalid or expired verification token"));

        if (user.isEmailVerified()) {
            throw new ValidationException("Email already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getId());
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resend verification email attempt for: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ValidationException("User not found"));

        if (user.isEmailVerified()) {
            throw new ValidationException("Email already verified");
        }

        String verificationToken = generateVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        log.info("Verification email resent to: {}", email);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .user(UserResponse.fromEntity(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .refreshExpiresIn(refreshTokenExpiration / 1000)
                .build();
    }

    private void saveRefreshToken(User user, String token) {
        // Revoke all existing tokens for user
        refreshTokenRepository.revokeAllByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token saved for user: {}", user.getId());
    }

    private String generateVerificationToken(User user) {
        // In production, use a proper JWT with expiration
        return UUID.nameUUIDFromBytes((user.getId() + user.getEmail()).getBytes()).toString();
    }
}