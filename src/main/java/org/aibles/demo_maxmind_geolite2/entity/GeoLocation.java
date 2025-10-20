package org.aibles.demo_maxmind_geolite2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents geographic location information extracted from IP address
 * Used for audit logging and security tracking in QR login feature
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocation {
    
    /**
     * Original IP address that was looked up
     */
    private String ipAddress;
    
    /**
     * ISO country code (e.g., "VN", "SG", "JP")
     */
    private String countryCode;
    
    /**
     * Full country name (e.g., "Vietnam", "Singapore", "Japan")
     */
    private String countryName;
    
    /**
     * City name where IP is located
     */
    private String city;
    
    /**
     * State/Province/Region name
     */
    private String region;
    
    /**
     * Postal/ZIP code
     */
    private String postalCode;
    
    /**
     * Latitude coordinate
     */
    private Double latitude;
    
    /**
     * Longitude coordinate
     */
    private Double longitude;
    
    /**
     * Timezone identifier (e.g., "Asia/Ho_Chi_Minh")
     */
    private String timezone;
    
    /**
     * Internet Service Provider name
     */
    private String isp;
    
    /**
     * Organization name associated with the IP
     */
    private String organization;
    
    /**
     * Indicates if this IP is from a known proxy/VPN
     */
    private boolean isProxy;
    
    /**
     * Accuracy radius in kilometers
     */
    private Integer accuracyRadius;
    
    /**
     * Timestamp when this location was resolved
     */
    private LocalDateTime resolvedAt;
    
    /**
     * Check if location data is available for Asia-Pacific region
     * @return true if location is in supported APAC countries
     */
    public boolean isApacRegion() {
        if (countryCode == null) return false;
        return countryCode.matches("VN|SG|JP|TH|MY|ID|PH|KR|TW|HK|IN|AU|NZ");
    }
    
    /**
     * Get formatted location string for logging
     * @return formatted string like "Ho Chi Minh City, Vietnam (VN)"
     */
    public String getFormattedLocation() {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        if (countryName != null && !countryName.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(countryName);
        }
        if (countryCode != null && !countryCode.isEmpty()) {
            sb.append(" (").append(countryCode).append(")");
        }
        return sb.toString();
    }
}