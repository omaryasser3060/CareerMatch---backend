package com.example.backend.service;

import com.example.backend.dto.response.CVProfileResponse;
import com.example.backend.dto.response.CVResponse;
import com.example.backend.dto.response.CVStatusResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.ValidationException;
import com.example.backend.model.CV;
import com.example.backend.model.User;
import com.example.backend.repository.CVRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CVService {

    private final CVRepository cvRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final PDFExtractionService pdfExtractionService;
    private final ObjectMapper objectMapper;
    private final CVProcessingService cvProcessingService;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    // ============================================================
    // Upload
    // ============================================================

    @Transactional
    public CVResponse uploadCV(String userId, MultipartFile file) {
        log.info("CV upload attempt for user: {}, filename: {}", userId, file.getOriginalFilename());

        User user = userService.findUserById(userId);

        validateFile(file);

        try {
            // Save file
            String fileUrl = fileStorageService.saveFile(file, userId);

            // Create CV record
            CV cv = CV.builder()
                    .user(user)
                    .filename(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .parsed(false)
                    .build();

            cv = cvRepository.save(cv);
            log.info("CV record created: {} for user: {}", cv.getId(), userId);

            // Trigger async extraction and AI profile processing
            cvProcessingService.processCVAsync(cv.getId());

            return CVResponse.fromEntity(cv);

        } catch (IOException e) {
            log.error("Failed to save CV file for user: {}", userId, e);
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // Async Extraction
    // ============================================================

    /**
     * Kept as a small compatibility wrapper for existing callers.
     * The actual @Async work lives in CVProcessingService so Spring can
     * invoke it through a proxy instead of using self-invocation.
     */
    public void extractCVContentAsync(String cvId) {
        cvProcessingService.processCVAsync(cvId);
    }

    // ============================================================
    // Get CV
    // ============================================================

    @Transactional(readOnly = true)
    public CVResponse getCV(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));
        return CVResponse.fromEntity(cv);
    }

    @Transactional(readOnly = true)
    public Page<CVResponse> getUserCVs(String userId, Pageable pageable) {
        return cvRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable)
                .map(CVResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public CVProfileResponse getCVProfile(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        if (!cv.isParsed()) {
            throw new ValidationException("CV is still being processed");
        }

        if (cv.getProfileJson() == null || cv.getProfileJson().isBlank()) {
            throw new ValidationException("CV profile not yet available");
        }

        try {
            CVProfileResponse profile = objectMapper.readValue(cv.getProfileJson(), CVProfileResponse.class);
            profile.setCvId(cv.getId());
            profile.setExtractionConfidence(cv.getExtractionConfidence());
            return profile;
        } catch (Exception e) {
            log.error("Failed to parse CV profile JSON for CV: {}", cvId, e);
            throw new RuntimeException("Failed to parse CV profile", e);
        }
    }

    @Transactional(readOnly = true)
    public CVStatusResponse getCVStatus(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        return CVStatusResponse.builder()
                .cvId(cv.getId())
                .parsed(cv.isParsed())
                .extractionConfidence(cv.getExtractionConfidence())
                .uploadedAt(cv.getUploadedAt())
                .parsedAt(cv.getParsedAt())
                .build();
    }

    // ============================================================
    // Reparse
    // ============================================================

    @Transactional
    public CVResponse reparseCV(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        log.info("Reparse CV: {}", cvId);

        cv.setParsed(false);
        cv.setParsedAt(null);
        cv.setRawText(null);
        cv.setSkillsJson(null);
        cv.setProfileJson(null);
        cv.setExtractionConfidence(null);
        cvRepository.save(cv);

        cvProcessingService.processCVAsync(cvId);

        return CVResponse.fromEntity(cv);
    }

    // ============================================================
    // Download
    // ============================================================

    @Transactional(readOnly = true)
    public byte[] downloadCV(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        try {
            return fileStorageService.readFile(cv.getFileUrl());
        } catch (IOException e) {
            log.error("Failed to read CV file: {}", cvId, e);
            throw new RuntimeException("Failed to read CV file", e);
        }
    }

    // ============================================================
    // Delete
    // ============================================================

    @Transactional
    public void deleteCV(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        log.info("Delete CV: {} for user: {}", cvId, userId);

        // Delete file from storage
        fileStorageService.deleteFile(cv.getFileUrl());

        // Soft delete
        cvRepository.delete(cv);
    }

    // ============================================================
    // Get Entity (for other services)
    // ============================================================

    @Transactional(readOnly = true)
    public CV getCVEntity(String userId, String cvId) {
        return cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));
    }

    // ============================================================
    // Update (for AI service)
    // ============================================================

    @Transactional
    public void updateCVProfile(String cvId, String profileJson, String skillsJson, String confidence) {
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        cv.setProfileJson(profileJson);
        cv.setSkillsJson(skillsJson);
        cv.setExtractionConfidence(confidence);
        cv.setParsed(true);
        cv.setParsedAt(LocalDateTime.now());
        cvRepository.save(cv);
        log.info("CV profile updated: {}", cvId);
    }

    // ============================================================
    // Validation
    // ============================================================

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(PDF_CONTENT_TYPE)) {
            throw new ValidationException("Only PDF files are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds 5MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new ValidationException("File must have .pdf extension");
        }
    }
}