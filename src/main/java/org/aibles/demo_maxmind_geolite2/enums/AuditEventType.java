package org.aibles.demo_maxmind_geolite2.enums;

import lombok.Getter;

/**
 * Enumeration of audit event types for QR login tracking
 * Used for categorizing different types of authentication events
 */
@Getter
public enum AuditEventType {
    
    QR_SCAN("QR_SCAN", "QR Code Scanned"),
    LOGIN_SUCCESS("LOGIN_SUCCESS", "Login Successful"),
    LOGIN_FAILED("LOGIN_FAILED", "Login Failed"),
    QR_GENERATED("QR_GENERATED", "QR Code Generated");
    
    private final String code;
    private final String description;
    
    AuditEventType(String code, String description) {
        this.code = code;
        this.description = description;
    }

  public static AuditEventType fromCode(String code) {
        for (AuditEventType eventType : values()) {
            if (eventType.code.equals(code)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("Unknown audit event type code: " + code);
    }
}