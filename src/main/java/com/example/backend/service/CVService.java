package com.example.backend.service;

import com.example.backend.dto.response.CVProfileResponse;
import com.example.backend.dto.response.CVResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.ValidationException;
import com.example.backend.model.CV;
import com.example.backend.model.User;
import com.example.backend.repository.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CVService {

    private final CVRepository cvRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @Transactional
    public CVResponse uploadCV(String userId, MultipartFile file) {
        User user = userService.findUserById(userId);

        // Validate file
        if (file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new ValidationException("Only PDF files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ValidationException("File size exceeds 5MB limit");
        }

        try {
            // Save file
            String fileUrl = fileStorageService.saveFile(file, userId);

            // Create CV record
            CV cv = CV.builder()
                    .user(user)
                    .filename(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(contentType)
                    .parsed(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            cv = cvRepository.save(cv);

            // TODO: Trigger async extraction process
            // extractCVContent(cv);

            return CVResponse.fromEntity(cv);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    public CVProfileResponse getCVProfile(String userId, String cvId) {
        CV cv = cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        if (!cv.isParsed()) {
            throw new ValidationException("CV is still being processed");
        }

        // Parse profile JSON and return
        // This is simplified - in production, parse from JSON
        return CVProfileResponse.builder()
                .cvId(cv.getId())
                .build();
    }

    public List<CVResponse> getUserCVs(String userId) {
        return cvRepository.findByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(CVResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCV(String userId, String cvId) {
        if (!cvRepository.existsByIdAndUserId(cvId, userId)) {
            throw new ResourceNotFoundException("CV", "id", cvId);
        }

        // Delete file from storage
        CV cv = cvRepository.findById(cvId).orElseThrow();
        fileStorageService.deleteFile(cv.getFileUrl());

        cvRepository.deleteByIdAndUserId(cvId, userId);
    }

    public CV getCV(String userId, String cvId) {
        return cvRepository.findByIdAndUserId(cvId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));
    }

    @Transactional
    public void updateCVProfile(String cvId, String profileJson) {
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", "id", cvId));

        cv.setProfileJson(profileJson);
        cv.setParsed(true);
        cv.setParsedAt(LocalDateTime.now());
        cvRepository.save(cv);
    }
}