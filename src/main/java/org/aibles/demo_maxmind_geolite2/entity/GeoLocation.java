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
    
}