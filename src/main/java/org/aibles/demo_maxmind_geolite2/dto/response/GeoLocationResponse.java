package org.aibles.demo_maxmind_geolite2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;

/**
 * Response DTO for geographic location data
 * Simplified to contain only location information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationResponse {
    private String ip;
    private GeoLocation data;
}