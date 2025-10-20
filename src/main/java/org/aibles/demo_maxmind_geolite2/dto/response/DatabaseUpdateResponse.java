package org.aibles.demo_maxmind_geolite2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response DTO for database update operations
 * Simplified to contain only update-specific data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseUpdateResponse {
    private boolean updateSuccessful;
    private LocalDateTime timestamp;
    private String details;
}