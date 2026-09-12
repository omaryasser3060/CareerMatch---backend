package com.example.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token refresh request")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Size(max = 500, message = "Refresh token must not exceed 500 characters")
    @Schema(description = "JWT refresh token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}