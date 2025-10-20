package org.aibles.demo_maxmind_geolite2.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class GeoIpServiceConfig {
    
    @Value("${geoip.service.maxmind-license-key}")
    private String maxmindLicenseKey;
    
    @Value("${geoip.service.enable-caching}")
    private boolean enableCaching;
    
    @Value("${geoip.service.cache-max-size}")
    private int cacheMaxSize;
    
    @Value("${geoip.service.cache-expire-minutes}")
    private long cacheExpireMinutes;
}