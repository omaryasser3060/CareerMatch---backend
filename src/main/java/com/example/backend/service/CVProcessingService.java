package com.example.backend.service;

import com.example.backend.dto.ai.CandidateProfileDto;
import com.example.backend.service.ai.LLMService;
import com.example.backend.util.JsonUtils;
import com.example.backend.model.CV;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CVRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Runs CV parsing and AI profile extraction outside the upload request thread.
 * This is a separate Spring bean intentionally: calling an @Async method from
 * the same bean would bypass Spring's async proxy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CVProcessingService {

    private static final int MAX_LLM_INPUT_CHARS = 30_000;

    private final CVRepository cvRepository;
    private final PDFExtractionService pdfExtractionService;
    private final LLMService llmService;

    @Async("aiTaskExecutor")
    public void processCVAsync(String cvId) {
        log.info("Starting async CV processing for CV: {}", cvId);

        try {
            CV cv = loadCV(cvId);
            String rawText = pdfExtractionService.extractText(cv.getFileUrl());

            if (rawText == null || rawText.isBlank()) {
                markProcessingFailed(cvId, "LOW", "PDF contains no extractable text");
                return;
            }

            saveRawText(cvId, rawText);

            String llmInput = truncateForLLM(rawText);
            CandidateProfileDto profile = llmService.extractCandidateProfile(llmInput);

            if (profile == null) {
                throw new IllegalStateException("LLM returned an empty candidate profile");
            }

            String profileJson = JsonUtils.toJson(profile);
            String skillsJson = JsonUtils.toJson(safeList(profile.getSkills()));
            String confidence = normalizeConfidence(profile.getConfidence());

            markAsParsed(cvId, rawText, profileJson, skillsJson, confidence);

            log.info("CV processing completed successfully: {} (skills={}, confidence={})",
                    cvId,
                    profile.getSkills() != null ? profile.getSkills().size() : 0,
                    confidence);

        } catch (ResourceNotFoundException e) {
            log.error("CV not found during processing: {}", cvId, e);
        } catch (Exception e) {
            log.error("Failed to process CV: {}", cvId, e);
            markProcessingFailedSafely(cvId, "LOW", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    protected CV loadCV(String cvId) {
        return cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));
    }

    @Transactional
    protected void saveRawText(String cvId, String rawText) {
        cvRepository.updateRawText(cvId, rawText);
    }

    @Transactional
    protected void markAsParsed(
            String cvId,
            String rawText,
            String profileJson,
            String skillsJson,
            String confidence
    ) {
        int updated = cvRepository.markAsParsed(
                cvId,
                rawText,
                skillsJson,
                profileJson,
                confidence
        );

        if (updated == 0) {
            throw new ResourceNotFoundException("CV", "id", cvId);
        }
    }

    @Transactional
    protected void markProcessingFailed(String cvId, String confidence, String reason) {
        int updated = cvRepository.updateProcessingFailure(
                cvId,
                false,
                confidence
        );

        if (updated > 0) {
            log.warn("CV processing marked as failed: {} - {}", cvId, reason);
        }
    }

    private void markProcessingFailedSafely(String cvId, String confidence, String reason) {
        try {
            markProcessingFailed(cvId, confidence, reason != null ? reason : "Unknown processing error");
        } catch (Exception updateException) {
            log.error("Could not persist CV processing failure state for CV: {}", cvId, updateException);
        }
    }

    private String truncateForLLM(String text) {
        if (text.length() <= MAX_LLM_INPUT_CHARS) {
            return text;
        }

        log.warn("CV text is {} characters; truncating to {} for LLM processing",
                text.length(), MAX_LLM_INPUT_CHARS);
        return text.substring(0, MAX_LLM_INPUT_CHARS);
    }

    private List<String> safeList(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String normalizeConfidence(String confidence) {
        if (confidence == null || confidence.isBlank()) {
            return "LOW";
        }

        return switch (confidence.trim().toUpperCase()) {
            case "HIGH", "MEDIUM", "LOW" -> confidence.trim().toUpperCase();
            default -> "LOW";
        };
    }
}
