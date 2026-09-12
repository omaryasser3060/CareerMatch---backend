package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "Error type", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Error code for programmatic handling", example = "INVALID_INPUT")
    private String errorCode;

    @Schema(description = "Human-readable error message", example = "Validation failed")
    private String message;

    @Schema(description = "Request path", example = "/api/auth/register")
    private String path;

    @Schema(description = "Field-level validation errors")
    private Map<String, String> fieldErrors;

    @Schema(description = "List of detailed errors")
    private List<String> details;

    @Schema(description = "Timestamp of the error", example = "2024-01-15T10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Trace ID for debugging")
    private String traceId;
}