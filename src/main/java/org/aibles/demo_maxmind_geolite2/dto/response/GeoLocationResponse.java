package org.aibles.demo_maxmind_geolite2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationResponse {
    private boolean success;
    private String message;
    private String ip;
    private GeoLocation data;
}