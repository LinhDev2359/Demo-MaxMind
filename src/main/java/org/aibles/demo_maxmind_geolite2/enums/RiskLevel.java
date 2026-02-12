package org.aibles.demo_maxmind_geolite2.enums;

import lombok.Getter;

/**
 * Enumeration of risk levels for security assessment
 * Used for categorizing the risk level of authentication attempts
 */
@Getter
public enum RiskLevel {
    
    LOW("LOW", "Low Risk", 1),
    MEDIUM("MEDIUM", "Medium Risk", 2),
    HIGH("HIGH", "High Risk", 3);
    
    private final String code;
    private final String description;
    private final int severity;
    
    RiskLevel(String code, String description, int severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

  public static RiskLevel fromCode(String code) {
        for (RiskLevel riskLevel : values()) {
            if (riskLevel.code.equals(code)) {
                return riskLevel;
            }
        }
        throw new IllegalArgumentException("Unknown risk level code: " + code);
    }
    
    public boolean isHigherThan(RiskLevel other) {
        return this.severity > other.severity;
    }
    
    public boolean isLowerThan(RiskLevel other) {
        return this.severity < other.severity;
    }
}