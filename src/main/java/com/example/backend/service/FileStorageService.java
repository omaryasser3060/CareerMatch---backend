package com.example.backend.service;

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
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String userId) throws IOException {
        // Create user directory
        Path userDir = Paths.get(uploadDir, userId);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".pdf";

        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = userDir.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public void deleteFile(String fileUrl) {
        try {
            Path filePath = Paths.get(fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }

    public byte[] readFile(String fileUrl) throws IOException {
        Path filePath = Paths.get(fileUrl);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + fileUrl);
        }
        return Files.readAllBytes(filePath);
    }
}