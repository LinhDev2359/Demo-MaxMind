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
    
    public GeoLocationResponse getLocationInfo(String ipAddress, HttpServletRequest request) {
        String targetIp = determineTargetIp(ipAddress, request);
        
        if (!isValidIp(targetIp)) {
            return buildInvalidIpResponse();
        }
        
        Optional<GeoLocation> geoLocation = geoIpService.resolveLocation(targetIp);
        return responseService.buildLocationResponse(geoLocation, targetIp);
    }
    
    public HealthResponse getServiceHealth() {
        boolean serviceAvailable = geoIpService.isServiceAvailable();
        String databaseInfo = geoIpService.getDatabaseInfo();
        String updateStatus = databaseFacadeService.getUpdateStatus();
        
        return responseService.buildHealthResponse(serviceAvailable, databaseInfo, updateStatus);
    }
    
    public DatabaseUpdateResponse performDatabaseUpdate() {
        return databaseFacadeService.performUpdate();
    }
    
    private String determineTargetIp(String providedIp, HttpServletRequest request) {
        return providedIp != null ? providedIp : IpExtractorUtil.extractClientIp(request);
    }
    
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty();
    }
    
    private GeoLocationResponse buildInvalidIpResponse() {
        return GeoLocationResponse.builder()
                .success(false)
                .message("No IP address provided or detectable")
                .build();
    }
    
}