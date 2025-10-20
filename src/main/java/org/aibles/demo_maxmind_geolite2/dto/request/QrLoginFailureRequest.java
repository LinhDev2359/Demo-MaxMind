package org.aibles.demo_maxmind_geolite2.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for failed QR login events
 * Contains validation rules for secure QR authentication tracking
 */
@Data
public class QrLoginFailureRequest {
    
    @Size(max = 100, message = "User ID must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "User ID can only contain alphanumeric characters, hyphens, and underscores")
    private String userId;
    
    @NotBlank(message = "QR Code ID is required")
    @Size(min = 1, max = 200, message = "QR Code ID must be between 1 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "QR Code ID can only contain alphanumeric characters, hyphens, and underscores")
    private String qrCodeId;
    
    @NotBlank(message = "Session ID is required")
    @Size(min = 1, max = 100, message = "Session ID must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Session ID can only contain alphanumeric characters, hyphens, and underscores")
    private String sessionId;
    
    @Size(max = 500, message = "Error message must not exceed 500 characters")
    private String errorMessage;
}