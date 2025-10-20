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
    
    public GeoLocationResponse buildLocationResponse(Optional<GeoLocation> geoLocation, String targetIp) {
        if (geoLocation.isPresent()) {
            log.info("Location lookup successful for IP: {} -> {}", 
                targetIp, geoLocation.get().getFormattedLocation());
            
            return GeoLocationResponse.builder()
                    .success(true)
                    .data(geoLocation.get())
                    .message("Location resolved successfully")
                    .build();
        } else {
            return GeoLocationResponse.builder()
                    .success(false)
                    .message("Location not found for IP address")
                    .ip(targetIp)
                    .build();
        }
    }
    
    public AuditResponse buildAuditResponse(AuditLogEntry auditLog) {
        return AuditResponse.builder()
                .success(true)
                .auditLogId(auditLog.getLogId())
                .riskLevel(auditLog.getRiskLevel())
                .suspicious(auditLog.isSuspiciousLocation())
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
                .success(success)
                .message(success ? "Database update completed" : "Database update failed")
                .timestamp(LocalDateTime.now())
                .build();
    }
}