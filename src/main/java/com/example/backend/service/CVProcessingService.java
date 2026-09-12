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

import java.util.Collections;
import java.util.List;

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
        log.info("▶️ STARTING AI PROCESSING FOR CV: {}", cvId);

        try {
            CV cv = cvRepository.findById(cvId)
                    .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

            log.info("Step 1: Extracting text from PDF...");
            String rawText = pdfExtractionService.extractText(cv.getFileUrl());

            if (rawText == null || rawText.isBlank()) {
                throw new RuntimeException("PDF contains no extractable text or file is unreadable.");
            }

            cvRepository.updateRawText(cvId, rawText);

            log.info("Step 2: Sending text to Gemini API...");
            String llmInput = truncateForLLM(rawText);
            CandidateProfileDto profile = llmService.extractCandidateProfile(llmInput);

            if (profile == null) {
                throw new IllegalStateException("LLM returned an empty candidate profile");
            }

            log.info("Step 3: Saving parsed profile to database...");
            String profileJson = JsonUtils.toJson(profile);
            String skillsJson = JsonUtils.toJson(safeList(profile.getSkills()));
            String confidence = normalizeConfidence(profile.getConfidence());

            int updated = cvRepository.markAsParsed(cvId, rawText, skillsJson, profileJson, confidence);
            if (updated > 0) {
                log.info("✅ CV processing completed successfully: {}", cvId);
            } else {
                throw new RuntimeException("Failed to update CV record in DB");
            }

        } catch (Throwable t) {
            log.error("❌ Failed to process CV: {}", cvId, t);
            try {
                cvRepository.updateProcessingFailure(cvId, false, "LOW");
                log.info("⚠️ CV {} marked as FAILED in database.", cvId);
            } catch (Exception ex) {
                log.error("CRITICAL: Failed to write failure state to DB for CV: {}", cvId, ex);
            }
        }
    }

    private String truncateForLLM(String text) {
        if (text.length() <= MAX_LLM_INPUT_CHARS) {
            return text;
        }
        return text.substring(0, MAX_LLM_INPUT_CHARS);
    }

    private List<String> safeList(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private String normalizeConfidence(String confidence) {
        if (confidence == null || confidence.isBlank()) {
            return "LOW";
        }
        String upper = confidence.trim().toUpperCase();
        return switch (upper) {
            case "HIGH", "MEDIUM", "LOW" -> upper;
            default -> "LOW";
        };
    }
}