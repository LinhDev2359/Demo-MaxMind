package org.aibles.demo_maxmind_geolite2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit log entry for QR login events with geographic information
 * Used for security monitoring and compliance tracking
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {
    
    /**
     * Unique identifier for this audit log entry
     */
    private String logId;
    
    /**
     * User identifier who attempted login
     */
    private String userId;
    
    /**
     * Session or device identifier
     */
    private String sessionId;
    
    /**
     * QR code identifier that was scanned
     */
    private String qrCodeId;
    
    /**
     * Type of event (QR_SCAN, LOGIN_SUCCESS, LOGIN_FAILED, etc.)
     */
    private String eventType;
    
    /**
     * Geographic location information derived from IP
     */
    private GeoLocation geoLocation;
    
    /**
     * User agent string from the request
     */
    private String userAgent;
    
    /**
     * Additional metadata as JSON string
     */
    private String metadata;
    
    /**
     * Timestamp when event occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Risk level assessment (LOW, MEDIUM, HIGH)
     */
    private String riskLevel;
    
    /**
     * Whether this login attempt was successful
     */
    private boolean success;
    
    /**
     * Error message if login failed
     */
    private String errorMessage;
    
    /**
     * Check if this login attempt is from a suspicious location
     * @return true if location appears suspicious
     */
    public boolean isSuspiciousLocation() {
        if (geoLocation == null) return true;
        
        // Flag as suspicious if:
        // 1. Using proxy/VPN
        // 2. Outside APAC region
        // 3. Low accuracy (> 50km radius)
        return geoLocation.isProxy() || 
               !geoLocation.isApacRegion() ||
               (geoLocation.getAccuracyRadius() != null && geoLocation.getAccuracyRadius() > 50);
    }
    
    /**
     * Generate log message for audit trail
     * @return formatted audit log message
     */
    public String generateLogMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("QR Login Event: ").append(eventType);
        sb.append(" | User: ").append(userId);
        sb.append(" | Success: ").append(success);
        
        if (geoLocation != null) {
            sb.append(" | Location: ").append(geoLocation.getFormattedLocation());
            sb.append(" | IP: ").append(geoLocation.getIpAddress());
        }
        
        if (isSuspiciousLocation()) {
            sb.append(" | SUSPICIOUS");
        }
        
        return sb.toString();
    }
}