package org.aibles.demo_maxmind_geolite2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;

/**
 * Response DTO for audit logging operations
 * Simplified to contain only audit-specific data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {
    private boolean success;
    private String auditLogId;
    private String riskLevel;
    private boolean suspicious;
    private GeoLocation location;
}