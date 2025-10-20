package org.aibles.demo_maxmind_geolite2.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Configuration
public class GeoIpUpdateConfig {
    
    @Value("${geoip.update.enabled}")
    private boolean enabled;
    
    @Value("${geoip.update.interval}")
    private Duration interval;
    
    @Value("${geoip.update.timeout}")
    private Duration timeout;
    
    @Value("${geoip.update.retry-attempts}")
    private int retryAttempts;
    
    @Value("${geoip.update.check-on-startup}")
    private boolean checkOnStartup;
}