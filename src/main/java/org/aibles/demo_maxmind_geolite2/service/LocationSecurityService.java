package org.aibles.demo_maxmind_geolite2.service;

import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;
import org.aibles.demo_maxmind_geolite2.enums.ApacCountry;
import org.aibles.demo_maxmind_geolite2.enums.RiskLevel;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating location-based security concerns
 * Handles business logic related to geographic risk assessment
 */
@Service
public class LocationSecurityService {
    
    /**
     * Check if this login attempt is from a suspicious location
     * @param geoLocation the geographic location to evaluate
     * @return true if location appears suspicious
     */
    public boolean isSuspiciousLocation(GeoLocation geoLocation) {
        if (geoLocation == null) return true;
        
        // Flag as suspicious if:
        // 1. Using proxy/VPN
        // 2. Outside APAC region
        // 3. Low accuracy (> 50km radius)
        return geoLocation.isProxy() || 
               !isApacRegion(geoLocation) ||
               (geoLocation.getAccuracyRadius() != null && geoLocation.getAccuracyRadius() > 50);
    }
    
    /**
     * Check if location data is available for Asia-Pacific region
     * @param geoLocation the geographic location to check
     * @return true if location is in supported APAC countries
     */
    public boolean isApacRegion(GeoLocation geoLocation) {
        if (geoLocation == null || geoLocation.getCountryCode() == null) return false;
        return ApacCountry.isApacCountry(geoLocation.getCountryCode());
    }
    
    /**
     * Assess risk level based on location characteristics
     * @param geoLocation the geographic location to evaluate
     * @return risk level as string (LOW, MEDIUM, HIGH)
     */
    public String assessRiskLevel(GeoLocation geoLocation) {
        if (geoLocation == null) return RiskLevel.HIGH.getCode();
        
        if (geoLocation.isProxy()) return RiskLevel.HIGH.getCode();
        if (!isApacRegion(geoLocation)) return RiskLevel.MEDIUM.getCode();
        if (geoLocation.getAccuracyRadius() != null && geoLocation.getAccuracyRadius() > 50) return RiskLevel.MEDIUM.getCode();
        
        return RiskLevel.LOW.getCode();
    }
}