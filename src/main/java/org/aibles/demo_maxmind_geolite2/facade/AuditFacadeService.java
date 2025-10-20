package org.aibles.demo_maxmind_geolite2.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.entity.AuditLogEntry;
import org.aibles.demo_maxmind_geolite2.dto.request.*;
import org.aibles.demo_maxmind_geolite2.dto.response.AuditResponse;
import org.aibles.demo_maxmind_geolite2.service.AuditLoggingService;
import org.aibles.demo_maxmind_geolite2.service.GeoIpResponseService;
import org.aibles.demo_maxmind_geolite2.util.IpExtractorUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditFacadeService {
    
    private final AuditLoggingService auditLoggingService;
    private final GeoIpResponseService responseService;
    
    public AuditResponse processQrScanEvent(QrScanRequest request, HttpServletRequest httpRequest) {
        String clientIp = IpExtractorUtil.extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        AuditLogEntry auditLog = auditLoggingService.logQrScanEvent(
            request.getUserId(),
            request.getQrCodeId(),
            clientIp,
            userAgent,
            request.getSessionId()
        );
        
        return responseService.buildAuditResponse(auditLog);
    }
    
    public AuditResponse processLoginSuccess(QrLoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = IpExtractorUtil.extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        AuditLogEntry auditLog = auditLoggingService.logSuccessfulLogin(
            request.getUserId(),
            request.getQrCodeId(),
            clientIp,
            userAgent,
            request.getSessionId()
        );
        
        return responseService.buildAuditResponse(auditLog);
    }
    
    public AuditResponse processLoginFailure(QrLoginFailureRequest request, HttpServletRequest httpRequest) {
        String clientIp = IpExtractorUtil.extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        AuditLogEntry auditLog = auditLoggingService.logFailedLogin(
            request.getUserId(),
            request.getQrCodeId(),
            clientIp,
            userAgent,
            request.getSessionId(),
            request.getErrorMessage()
        );
        
        return responseService.buildAuditResponse(auditLog);
    }
}