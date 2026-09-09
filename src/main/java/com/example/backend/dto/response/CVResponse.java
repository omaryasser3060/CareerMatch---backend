package com.example.backend.dto.response;

import com.example.backend.model.CV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVResponse {

    private String id;
    private String filename;
    private String fileUrl;
    private LocalDateTime uploadedAt;
    private LocalDateTime parsedAt;
    private boolean parsed;
    private Long fileSize;
    private String fileType;

    public static CVResponse fromEntity(CV cv) {
        return CVResponse.builder()
                .id(cv.getId())
                .filename(cv.getFilename())
                .fileUrl(cv.getFileUrl())
                .uploadedAt(cv.getUploadedAt())
                .parsedAt(cv.getParsedAt())
                .parsed(cv.isParsed())
                .fileSize(cv.getFileSize())
                .fileType(cv.getFileType())
                .build();
    }
}