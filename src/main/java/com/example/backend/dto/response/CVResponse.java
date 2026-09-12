package com.example.backend.dto.response;

import com.example.backend.model.CV;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "CV response")
public class CVResponse {

    @Schema(description = "CV ID")
    private String id;

    @Schema(description = "Original filename", example = "resume.pdf")
    private String filename;

    @Schema(description = "File URL")
    private String fileUrl;

    @Schema(description = "File size in bytes", example = "102400")
    private Long fileSize;

    @Schema(description = "File MIME type", example = "application/pdf")
    private String fileType;

    @Schema(description = "Whether the CV has been parsed", example = "true")
    private boolean parsed;

    @Schema(description = "Extraction confidence", example = "HIGH")
    private String extractionConfidence;

    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadedAt;

    @Schema(description = "Parsing timestamp")
    private LocalDateTime parsedAt;

    public static CVResponse fromEntity(CV cv) {
        return CVResponse.builder()
                .id(cv.getId())
                .filename(cv.getFilename())
                .fileUrl(cv.getFileUrl())
                .fileSize(cv.getFileSize())
                .fileType(cv.getFileType())
                .parsed(cv.isParsed())
                .extractionConfidence(cv.getExtractionConfidence())
                .uploadedAt(cv.getUploadedAt())
                .parsedAt(cv.getParsedAt())
                .build();
    }
}