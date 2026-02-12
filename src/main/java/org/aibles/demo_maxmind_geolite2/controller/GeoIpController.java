package org.aibles.demo_maxmind_geolite2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.dto.request.*;
import org.aibles.demo_maxmind_geolite2.dto.response.*;
import org.aibles.demo_maxmind_geolite2.facade.AuditFacadeService;
import org.aibles.demo_maxmind_geolite2.facade.GeoIpFacadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST API controller for GeoIP services
 * Provides endpoints for IP geolocation lookup and QR login audit logging
 * Designed for integration with multi-country QR authentication systems
 */
@Slf4j
@RestController
@RequestMapping("/api/geoip")
@RequiredArgsConstructor
@Validated
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
    public ResponseEntity<ApiResponse<GeoLocationResponse>> getLocation(
            @RequestParam(required = false) String ipAddress,
            HttpServletRequest request) {
        
        try {
            ApiResponse<GeoLocationResponse> response = geoIpFacadeService.getLocationInfo(ipAddress, request);
            
            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing location request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage()));
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
    public ResponseEntity<ApiResponse<AuditResponse>> logQrScan(
            @Valid @RequestBody QrScanRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            ApiResponse<AuditResponse> response = auditFacadeService.processQrScanEvent(request, httpRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error logging QR scan event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage()));
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
    public ResponseEntity<ApiResponse<AuditResponse>> logLoginSuccess(
            @Valid @RequestBody QrLoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            ApiResponse<AuditResponse> response = auditFacadeService.processLoginSuccess(request, httpRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error logging login success", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage()));
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
    public ResponseEntity<ApiResponse<AuditResponse>> logLoginFailed(
            @Valid @RequestBody QrLoginFailureRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            ApiResponse<AuditResponse> response = auditFacadeService.processLoginFailure(request, httpRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error logging login failure", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Get service health and database status
     * 
     * @return ResponseEntity containing service status information
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        try {
            ApiResponse<HealthResponse> response = geoIpFacadeService.getServiceHealth();
            
            HttpStatus status = response.getData().isServiceAvailable() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            log.error("Error checking service health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Force database update (admin endpoint)
     * 
     * @return ResponseEntity indicating update result
     */
    @PostMapping("/admin/update-database")
    public ResponseEntity<ApiResponse<DatabaseUpdateResponse>> updateDatabase() {
        try {
            ApiResponse<DatabaseUpdateResponse> response = geoIpFacadeService.performDatabaseUpdate();
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during manual database update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Database update failed: " + e.getMessage()));
        }
    }
    
}