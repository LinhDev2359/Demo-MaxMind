package org.aibles.demo_maxmind_geolite2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response DTO for service health status
 * Contains service availability and database information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private boolean serviceAvailable;
    private String databaseInfo;
    private String updateStatus;
    private LocalDateTime timestamp;
}