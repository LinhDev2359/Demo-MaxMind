package org.aibles.demo_maxmind_geolite2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.entity.AuditLogEntry;
import org.aibles.demo_maxmind_geolite2.dto.response.*;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIpResponseService {
    
    private final AuditLogFormatter auditLogFormatter;
    private final LocationSecurityService locationSecurityService;
    
    public GeoLocationResponse buildLocationResponse(Optional<GeoLocation> geoLocation, String targetIp) {
        if (geoLocation.isPresent()) {
            log.info("Location lookup successful for IP: {} -> {}", 
                targetIp, auditLogFormatter.getFormattedLocation(geoLocation.get()));
            
            return GeoLocationResponse.builder()
                    .ip(targetIp)
                    .data(geoLocation.get())
                    .build();
        } else {
            return GeoLocationResponse.builder()
                    .ip(targetIp)
                    .data(null)
                    .build();
        }
    }
    
    public AuditResponse buildAuditResponse(AuditLogEntry auditLog) {
        return AuditResponse.builder()
                .auditLogId(auditLog.getLogId())
                .riskLevel(auditLog.getRiskLevel())
                .suspicious(locationSecurityService.isSuspiciousLocation(auditLog.getGeoLocation()))
                .location(auditLog.getGeoLocation())
                .build();
    }
    
    public HealthResponse buildHealthResponse(boolean serviceAvailable, String databaseInfo, String updateStatus) {
        return HealthResponse.builder()
                .serviceAvailable(serviceAvailable)
                .databaseInfo(databaseInfo)
                .updateStatus(updateStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public DatabaseUpdateResponse buildUpdateResponse(boolean success) {
        return DatabaseUpdateResponse.builder()
                .updateSuccessful(success)
                .details(success ? "Database update completed successfully" : "Database update operation failed")
                .timestamp(LocalDateTime.now())
                .build();
    }
}