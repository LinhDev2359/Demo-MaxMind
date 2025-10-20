package org.aibles.demo_maxmind_geolite2.dto.request;

import lombok.Data;

@Data
public class QrScanRequest {
    private String userId;
    private String qrCodeId;
    private String sessionId;
}