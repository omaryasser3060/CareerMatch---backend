package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.CVProfileResponse;
import com.example.backend.dto.response.CVResponse;
import com.example.backend.dto.response.CVStatusResponse;
import com.example.backend.service.CVService;
import com.example.backend.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "CV Management", description = "CV upload, parsing, and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CVController {

    private final CVService cvService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a CV", description = "Uploads a PDF CV and extracts text from it")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "CV uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or file type"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "File too large"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CVResponse>> uploadCV(
            @Parameter(description = "PDF file to upload", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("CV upload attempt for user: {}, filename: {}, size: {}",
                userId, file.getOriginalFilename(), file.getSize());

        CVResponse response = cvService.uploadCV(userId, file);
        log.info("CV uploaded successfully: {} for user: {}", response.getId(), userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("CV uploaded successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all user CVs", description = "Returns a paginated list of the current user's CVs")
    public ResponseEntity<ApiResponse<Page<CVResponse>>> getUserCVs(
            @PageableDefault(size = 10, sort = "uploadedAt") Pageable pageable
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        Page<CVResponse> response = cvService.getUserCVs(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{cvId}")
    @Operation(summary = "Get CV by ID", description = "Returns a specific CV by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CV found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CV not found")
    })
    public ResponseEntity<ApiResponse<CVResponse>> getCV(
            @PathVariable @NotBlank String cvId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        CVResponse response = cvService.getCV(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{cvId}/profile")
    @Operation(summary = "Get CV profile", description = "Returns the parsed profile of a CV")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CV not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "CV not yet parsed")
    })
    public ResponseEntity<ApiResponse<CVProfileResponse>> getCVProfile(
            @PathVariable @NotBlank String cvId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        CVProfileResponse response = cvService.getCVProfile(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{cvId}/status")
    @Operation(summary = "Get CV parsing status", description = "Returns the parsing status of a CV")
    public ResponseEntity<ApiResponse<CVStatusResponse>> getCVStatus(
            @PathVariable @NotBlank String cvId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        CVStatusResponse response = cvService.getCVStatus(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{cvId}/reparse")
    @Operation(summary = "Reparse CV", description = "Triggers a re-parse of the CV")
    public ResponseEntity<ApiResponse<CVResponse>> reparseCV(
            @PathVariable @NotBlank String cvId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Reparse attempt for CV: {} by user: {}", cvId, userId);
        CVResponse response = cvService.reparseCV(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success("CV re-parsed successfully", response));
    }

    @DeleteMapping("/{cvId}")
    @Operation(summary = "Delete CV", description = "Deletes a CV and its associated data")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "CV deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CV not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCV(
            @PathVariable @NotBlank String cvId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("Delete attempt for CV: {} by user: {}", cvId, userId);
        cvService.deleteCV(userId, cvId);
        return ResponseEntity.ok(ApiResponse.success("CV deleted successfully", null));
    }

    @GetMapping("/{cvId}/download")
    @Operation(summary = "Download CV file", description = "Downloads the original CV PDF file")
    public ResponseEntity<byte[]> downloadCV(
            @PathVariable @NotBlank String cvId
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        byte[] fileContent = cvService.downloadCV(userId, cvId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"cv.pdf\"")
                .body(fileContent);
    }
}