package org.aibles.demo_maxmind_geolite2.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.dto.response.*;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;
import org.aibles.demo_maxmind_geolite2.service.GeoIpResponseService;
import org.aibles.demo_maxmind_geolite2.service.GeoIpService;
import org.aibles.demo_maxmind_geolite2.util.IpExtractorUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIpFacadeService {
    
    private final GeoIpService geoIpService;
    private final GeoIpResponseService responseService;
    private final DatabaseFacadeService databaseFacadeService;
    
    public ApiResponse<GeoLocationResponse> getLocationInfo(String ipAddress, HttpServletRequest request) {
        String targetIp = determineTargetIp(ipAddress, request);
        
        if (!isValidIp(targetIp)) {
            return ApiResponse.error("No IP address provided or detectable");
        }
        
        Optional<GeoLocation> geoLocation = geoIpService.resolveLocation(targetIp);
        GeoLocationResponse response = responseService.buildLocationResponse(geoLocation, targetIp);
        
        if (response.getData() == null) {
            return ApiResponse.error("No location data available for IP: " + response.getIp());
        }
        
        return ApiResponse.success(response, "Location retrieved successfully");
    }
    
    public ApiResponse<HealthResponse> getServiceHealth() {
        boolean serviceAvailable = geoIpService.isServiceAvailable();
        String databaseInfo = geoIpService.getDatabaseInfo();
        String updateStatus = databaseFacadeService.getUpdateStatus();
        
        HealthResponse response = responseService.buildHealthResponse(serviceAvailable, databaseInfo, updateStatus);
        String message = serviceAvailable ? "Service is healthy" : "Service is unavailable";
        
        return ApiResponse.success(response, message);
    }
    
    public ApiResponse<DatabaseUpdateResponse> performDatabaseUpdate() {
        DatabaseUpdateResponse response = databaseFacadeService.performUpdate();
        String message = response.isUpdateSuccessful() ? "Database updated successfully" : "Database update failed";
        
        return ApiResponse.success(response, message);
    }
    
    private String determineTargetIp(String providedIp, HttpServletRequest request) {
        return providedIp != null ? providedIp : IpExtractorUtil.extractClientIp(request);
    }
    
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty();
    }
    
    private GeoLocationResponse buildInvalidIpResponse() {
        return GeoLocationResponse.builder()
                .ip(null)
                .data(null)
                .build();
    }
    
}