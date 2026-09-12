package com.example.backend.service;

import com.example.backend.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file.upload-dir:./uploads/cvs}")
    private String uploadDir;

    @Value("${app.file.allowed-extensions:pdf}")
    private String allowedExtensions;

    public String saveFile(MultipartFile file, String userId) throws IOException {
        // Validate filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("Filename is required");
        }

        // Sanitize filename (prevent path traversal)
        String sanitizedFilename = Paths.get(originalFilename).getFileName().toString();

        // Validate extension
        String extension = getExtension(sanitizedFilename);
        if (!isAllowedExtension(extension)) {
            throw new ValidationException("File extension not allowed: " + extension);
        }

        // Create user directory
        Path userDir = Paths.get(uploadDir, sanitizeUserId(userId)).normalize().toAbsolutePath();
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // Generate unique filename
        String uniqueFilename = UUID.randomUUID() + "." + extension;
        Path filePath = userDir.resolve(uniqueFilename).normalize();

        // Ensure file is within userDir (prevent path traversal)
        if (!filePath.startsWith(userDir)) {
            throw new ValidationException("Invalid file path");
        }

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File saved: {} for user: {}", filePath, userId);

        return filePath.toString();
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            Path filePath = Paths.get(fileUrl).normalize().toAbsolutePath();
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted: {}", fileUrl);
            } else {
                log.warn("File not found for deletion: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    public byte[] readFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new ValidationException("File URL is required");
        }

        Path filePath = Paths.get(fileUrl).normalize().toAbsolutePath();
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileUrl);
        }

        return Files.readAllBytes(filePath);
    }

    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }
        Path filePath = Paths.get(fileUrl).normalize().toAbsolutePath();
        return Files.exists(filePath);
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String extension) {
        String[] allowed = allowedExtensions.split(",");
        for (String ext : allowed) {
            if (ext.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeUserId(String userId) {
        // Remove any characters that could be used for path traversal
        return userId.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }
}