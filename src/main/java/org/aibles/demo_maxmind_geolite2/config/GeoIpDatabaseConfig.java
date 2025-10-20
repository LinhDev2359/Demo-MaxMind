package org.aibles.demo_maxmind_geolite2.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class GeoIpDatabaseConfig {
    
    @Value("${geoip.database.path}")
    private String path;
    
    @Value("${geoip.database.download-url}")
    private String downloadUrl;
    
    @Value("${geoip.database.edition-id}")
    private String editionId;
    
    @Value("${geoip.database.suffix}")
    private String suffix;
}