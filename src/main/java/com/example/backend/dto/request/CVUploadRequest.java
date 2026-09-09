package com.example.backend.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CVUploadRequest {

    private MultipartFile file;
}