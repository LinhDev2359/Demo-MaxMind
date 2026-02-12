package org.aibles.demo_maxmind_geolite2.service;

import org.aibles.demo_maxmind_geolite2.entity.AuditLogEntry;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for formatting audit logs and location information
 * Handles presentation logic for logging and display purposes
 */
@Service
public class AuditLogFormatter {
    
    @Autowired
    private LocationSecurityService locationSecurityService;
    
    /**
     * Generate log message for audit trail
     * @param auditLogEntry the audit log entry to format
     * @return formatted audit log message
     */
    public String generateLogMessage(AuditLogEntry auditLogEntry) {
        StringBuilder sb = new StringBuilder();
        sb.append("QR Login Event: ").append(auditLogEntry.getEventType());
        sb.append(" | User: ").append(auditLogEntry.getUserId());
        sb.append(" | Success: ").append(auditLogEntry.isSuccess());
        
        if (auditLogEntry.getGeoLocation() != null) {
            sb.append(" | Location: ").append(getFormattedLocation(auditLogEntry.getGeoLocation()));
            sb.append(" | IP: ").append(auditLogEntry.getGeoLocation().getIpAddress());
        }
        
        if (locationSecurityService.isSuspiciousLocation(auditLogEntry.getGeoLocation())) {
            sb.append(" | SUSPICIOUS");
        }
        
        return sb.toString();
    }
    
    /**
     * Get formatted location string for logging
     * @param geoLocation the geographic location to format
     * @return formatted string like "Ho Chi Minh City, Vietnam (VN)"
     */
    public String getFormattedLocation(GeoLocation geoLocation) {
        if (geoLocation == null) return "Unknown Location";
        
        StringBuilder sb = new StringBuilder();
        if (geoLocation.getCity() != null && !geoLocation.getCity().isEmpty()) {
            sb.append(geoLocation.getCity());
        }
        if (geoLocation.getCountryName() != null && !geoLocation.getCountryName().isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(geoLocation.getCountryName());
        }
        if (geoLocation.getCountryCode() != null && !geoLocation.getCountryCode().isEmpty()) {
            sb.append(" (").append(geoLocation.getCountryCode()).append(")");
        }
        return sb.toString();
    }
    
    /**
     * Format location with additional security context
     * @param geoLocation the geographic location to format
     * @return formatted string with security indicators
     */
    public String getFormattedLocationWithSecurity(GeoLocation geoLocation) {
        String location = getFormattedLocation(geoLocation);
        
        if (geoLocation != null) {
            if (geoLocation.isProxy()) {
                location += " [PROXY/VPN]";
            }
            if (!locationSecurityService.isApacRegion(geoLocation)) {
                location += " [NON-APAC]";
            }
            if (geoLocation.getAccuracyRadius() != null && geoLocation.getAccuracyRadius() > 50) {
                location += " [LOW_ACCURACY]";
            }
        }
        
        return location;
    }
}