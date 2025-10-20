package org.aibles.demo_maxmind_geolite2.dto.request;

import lombok.Data;

@Data
public class QrLoginRequest {
    private String userId;
    private String qrCodeId;
    private String sessionId;
}