package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.CVProfileResponse;
import com.example.backend.dto.response.CVResponse;
import com.example.backend.service.CVService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CVController {

    private final CVService cvService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CVResponse>> uploadCV(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        String userId = getUserId(authentication);
        CVResponse response = cvService.uploadCV(userId, file);
        return ResponseEntity.ok(ApiResponse.success("CV uploaded successfully", response));
    }

    @GetMapping("/{cvId}/profile")
    public ResponseEntity<ApiResponse<CVProfileResponse>> getCVProfile(
            Authentication authentication,
            @PathVariable String cvId
    ) {
        String userId = getUserId(authentication);
        CVProfileResponse response = cvService.getCVProfile(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CVResponse>>> getUserCVs(Authentication authentication) {
        String userId = getUserId(authentication);
        List<CVResponse> response = cvService.getUserCVs(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{cvId}/status")
    public ResponseEntity<ApiResponse<Object>> getCVStatus(
            Authentication authentication,
            @PathVariable String cvId
    ) {
        // Simplified status check
        return ResponseEntity.ok(ApiResponse.success("CV status retrieved"));
    }

    @DeleteMapping("/{cvId}")
    public ResponseEntity<ApiResponse<Void>> deleteCV(
            Authentication authentication,
            @PathVariable String cvId
    ) {
        String userId = getUserId(authentication);
        cvService.deleteCV(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success("CV deleted successfully", null));
    }

    private String getUserId(Authentication authentication) {
        // In production, extract user ID from authentication
        // For now, return a mock ID
        return "user-id";
    }
}