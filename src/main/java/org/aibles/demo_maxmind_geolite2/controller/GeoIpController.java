package org.aibles.demo_maxmind_geolite2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.dto.request.*;
import org.aibles.demo_maxmind_geolite2.dto.response.*;
import org.aibles.demo_maxmind_geolite2.facade.AuditFacadeService;
import org.aibles.demo_maxmind_geolite2.facade.GeoIpFacadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST API controller for GeoIP services
 * Provides endpoints for IP geolocation lookup and QR login audit logging
 * Designed for integration with multi-country QR authentication systems
 */
@Slf4j
@RestController
@RequestMapping("/api/geoip")
@RequiredArgsConstructor
public class GeoIpController {
    
    private final GeoIpFacadeService geoIpFacadeService;
    private final AuditFacadeService auditFacadeService;
    
    /**
     * Get geographic location information for an IP address
     * Primary endpoint for QR login location tracking
     * 
     * @param ipAddress IP address to lookup (optional, uses request IP if not provided)
     * @param request HTTP request for extracting client IP
     * @return ResponseEntity containing GeoLocation data or error
     */
    @GetMapping("/location")
    public ResponseEntity<GeoLocationResponse> getLocation(
            @RequestParam(required = false) String ipAddress,
            HttpServletRequest request) {
        
        try {
            GeoLocationResponse response = geoIpFacadeService.getLocationInfo(ipAddress, request);
            
            if (!response.isSuccess() && response.getMessage().contains("No IP address")) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing location request", e);
            GeoLocationResponse errorResponse = GeoLocationResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Log QR code scan event with geographic tracking
     * Used by QR authentication system to track scan locations
     * 
     * @param request QR scan event details
     * @param httpRequest HTTP request for IP extraction
     * @return ResponseEntity containing audit log entry
     */
    @PostMapping("/audit/qr-scan")
    public ResponseEntity<AuditResponse> logQrScan(
            @RequestBody QrScanRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            AuditResponse response = auditFacadeService.processQrScanEvent(request, httpRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error logging QR scan event", e);
            AuditResponse errorResponse = AuditResponse.builder()
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Log successful QR login event
     * 
     * @param request QR login success details
     * @param httpRequest HTTP request for IP extraction
     * @return ResponseEntity containing audit log entry
     */
    @PostMapping("/audit/login-success")
    public ResponseEntity<AuditResponse> logLoginSuccess(
            @RequestBody QrLoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            AuditResponse response = auditFacadeService.processLoginSuccess(request, httpRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error logging login success", e);
            AuditResponse errorResponse = AuditResponse.builder()
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Log failed QR login attempt
     * 
     * @param request QR login failure details
     * @param httpRequest HTTP request for IP extraction
     * @return ResponseEntity containing audit log entry
     */
    @PostMapping("/audit/login-failed")
    public ResponseEntity<AuditResponse> logLoginFailed(
            @RequestBody QrLoginFailureRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            AuditResponse response = auditFacadeService.processLoginFailure(request, httpRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error logging login failure", e);
            AuditResponse errorResponse = AuditResponse.builder()
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get service health and database status
     * 
     * @return ResponseEntity containing service status information
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        try {
            HealthResponse response = geoIpFacadeService.getServiceHealth();
            
            HttpStatus status = response.isServiceAvailable() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            log.error("Error checking service health", e);
            HealthResponse errorResponse = HealthResponse.builder()
                    .serviceAvailable(false)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Force database update (admin endpoint)
     * 
     * @return ResponseEntity indicating update result
     */
    @PostMapping("/admin/update-database")
    public ResponseEntity<DatabaseUpdateResponse> updateDatabase() {
        try {
            DatabaseUpdateResponse response = geoIpFacadeService.performDatabaseUpdate();
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during manual database update", e);
            DatabaseUpdateResponse errorResponse = DatabaseUpdateResponse.builder()
                    .success(false)
                    .message("Database update failed: " + e.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
}