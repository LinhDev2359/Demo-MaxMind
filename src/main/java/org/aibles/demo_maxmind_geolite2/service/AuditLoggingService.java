package org.aibles.demo_maxmind_geolite2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.entity.AuditLogEntry;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;
import org.aibles.demo_maxmind_geolite2.enums.AuditEventType;
import org.aibles.demo_maxmind_geolite2.enums.RiskLevel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for audit logging of QR login events with geographic tracking
 * Integrates with GeoIP service to provide location-aware security monitoring
 * Designed for compliance and security analysis in multi-country deployments
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLoggingService {
    
    private final GeoIpService geoIpService;
    private final LocationSecurityService locationSecurityService;
    private final AuditLogFormatter auditLogFormatter;
    
    /**
     * Log QR code scan event with geographic context
     * 
     * @param userId User identifier who scanned the QR code
     * @param qrCodeId QR code identifier that was scanned
     * @param ipAddress IP address of the scanning device
     * @param userAgent User agent string from the request
     * @param sessionId Session or device identifier
     * @return AuditLogEntry with complete event details
     */
    public AuditLogEntry logQrScanEvent(String userId, String qrCodeId, String ipAddress, 
                                       String userAgent, String sessionId) {
        
        GeoLocation geoLocation = geoIpService.resolveLocation(ipAddress).orElse(null);
        
        AuditLogEntry auditLog = AuditLogEntry.builder()
            .logId(generateLogId())
            .userId(userId)
            .qrCodeId(qrCodeId)
            .sessionId(sessionId)
            .eventType(AuditEventType.QR_SCAN.getCode())
            .geoLocation(geoLocation)
            .userAgent(userAgent)
            .timestamp(LocalDateTime.now())
            .success(true)
            .riskLevel(locationSecurityService.assessRiskLevel(geoLocation))
            .build();
        
        logAuditEvent(auditLog);
        return auditLog;
    }
    
    /**
     * Log successful QR login event
     * 
     * @param userId User identifier who logged in
     * @param qrCodeId QR code used for login
     * @param ipAddress IP address of login attempt
     * @param userAgent User agent string
     * @param sessionId Session identifier
     * @return AuditLogEntry with login success details
     */
    public AuditLogEntry logSuccessfulLogin(String userId, String qrCodeId, String ipAddress,
                                          String userAgent, String sessionId) {
        
        GeoLocation geoLocation = geoIpService.resolveLocation(ipAddress).orElse(null);
        
        AuditLogEntry auditLog = AuditLogEntry.builder()
            .logId(generateLogId())
            .userId(userId)
            .qrCodeId(qrCodeId)
            .sessionId(sessionId)
            .eventType(AuditEventType.LOGIN_SUCCESS.getCode())
            .geoLocation(geoLocation)
            .userAgent(userAgent)
            .timestamp(LocalDateTime.now())
            .success(true)
            .riskLevel(locationSecurityService.assessRiskLevel(geoLocation))
            .build();
        
        logAuditEvent(auditLog);
        
        // Log additional warning for suspicious locations
        if (locationSecurityService.isSuspiciousLocation(auditLog.getGeoLocation())) {
            logSecurityAlert(auditLog);
        }
        
        return auditLog;
    }
    
    /**
     * Log failed QR login attempt
     * 
     * @param userId User identifier (may be null for invalid attempts)
     * @param qrCodeId QR code identifier
     * @param ipAddress IP address of failed attempt
     * @param userAgent User agent string
     * @param sessionId Session identifier
     * @param errorMessage Reason for login failure
     * @return AuditLogEntry with failure details
     */
    public AuditLogEntry logFailedLogin(String userId, String qrCodeId, String ipAddress,
                                      String userAgent, String sessionId, String errorMessage) {
        
        GeoLocation geoLocation = geoIpService.resolveLocation(ipAddress).orElse(null);
        
        AuditLogEntry auditLog = AuditLogEntry.builder()
            .logId(generateLogId())
            .userId(userId != null ? userId : "UNKNOWN")
            .qrCodeId(qrCodeId)
            .sessionId(sessionId)
            .eventType(AuditEventType.LOGIN_FAILED.getCode())
            .geoLocation(geoLocation)
            .userAgent(userAgent)
            .timestamp(LocalDateTime.now())
            .success(false)
            .errorMessage(errorMessage)
            .riskLevel(assessRiskLevel(geoLocation, true))
            .build();
        
        logAuditEvent(auditLog);
        
        // Always log security alert for failed attempts from suspicious locations
        if (locationSecurityService.isSuspiciousLocation(auditLog.getGeoLocation())) {
            logSecurityAlert(auditLog);
        }
        
        return auditLog;
    }
    
    /**
     * Log QR code generation event for audit trail
     * 
     * @param qrCodeId Generated QR code identifier
     * @param userId User who requested QR code generation
     * @param ipAddress IP address of request
     * @param sessionId Session identifier
     * @return AuditLogEntry with QR generation details
     */
    public AuditLogEntry logQrGeneration(String qrCodeId, String userId, String ipAddress, String sessionId) {
        GeoLocation geoLocation = geoIpService.resolveLocation(ipAddress).orElse(null);
        
        AuditLogEntry auditLog = AuditLogEntry.builder()
            .logId(generateLogId())
            .userId(userId)
            .qrCodeId(qrCodeId)
            .sessionId(sessionId)
            .eventType(AuditEventType.QR_GENERATED.getCode())
            .geoLocation(geoLocation)
            .timestamp(LocalDateTime.now())
            .success(true)
            .riskLevel(RiskLevel.LOW.getCode())
            .build();
        
        logAuditEvent(auditLog);
        return auditLog;
    }
    
    /**
     * Assess risk level based on geographic location and login context
     * 
     * @param geoLocation Geographic location information
     * @param isFailedAttempt Whether this is a failed login attempt
     * @return Risk level string (LOW, MEDIUM, HIGH)
     */
    private String assessRiskLevel(GeoLocation geoLocation, boolean isFailedAttempt) {
        if (geoLocation == null) {
            return isFailedAttempt ? RiskLevel.HIGH.getCode() : RiskLevel.MEDIUM.getCode();
        }
        
        // High risk factors
        if (geoLocation.isProxy() || isFailedAttempt) {
            return RiskLevel.HIGH.getCode();
        }
        
        // Medium risk factors
        if (!locationSecurityService.isApacRegion(geoLocation) || 
            (geoLocation.getAccuracyRadius() != null && geoLocation.getAccuracyRadius() > 50)) {
            return RiskLevel.MEDIUM.getCode();
        }
        
        // Low risk for normal APAC region access
        return RiskLevel.LOW.getCode();
    }
    
    /**
     * Assess risk level for successful operations
     * 
     * @param geoLocation Geographic location information
     * @return Risk level string
     */
    private String assessRiskLevel(GeoLocation geoLocation) {
        return assessRiskLevel(geoLocation, false);
    }
    
    /**
     * Generate unique log identifier
     * 
     * @return Unique log ID string
     */
    private String generateLogId() {
        return "AUDIT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    /**
     * Write audit event to log with structured format
     * 
     * @param auditLog Audit log entry to write
     */
    private void logAuditEvent(AuditLogEntry auditLog) {
        log.info("AUDIT_LOG: {}", auditLogFormatter.generateLogMessage(auditLog));
        
        // In production, you might want to:
        // 1. Send to centralized logging system (ELK, Splunk)
        // 2. Store in database for compliance
        // 3. Send to SIEM for security monitoring
        // 4. Trigger alerts based on risk level
    }
    
    /**
     * Log security alert for suspicious activities
     * 
     * @param auditLog Audit log entry that triggered the alert
     */
    private void logSecurityAlert(AuditLogEntry auditLog) {
        log.warn("SECURITY_ALERT: Suspicious QR login activity detected - {}", 
            auditLogFormatter.generateLogMessage(auditLog));
            
        // In production, implement:
        // 1. Real-time alerting to security team
        // 2. Automated blocking of suspicious IPs
        // 3. Enhanced monitoring for the user/session
        // 4. Integration with fraud detection systems
    }
}