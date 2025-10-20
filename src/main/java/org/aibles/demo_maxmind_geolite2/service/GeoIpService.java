package org.aibles.demo_maxmind_geolite2.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.config.GeoIpDatabaseConfig;
import org.aibles.demo_maxmind_geolite2.entity.GeoLocation;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for resolving geographic location information from IP addresses
 * Uses MaxMind GeoLite2 database for high-performance, privacy-friendly IP geolocation
 * Optimized for Asia-Pacific region usage in multi-country applications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoIpService {
    
    private final GeoIpDatabaseConfig databaseConfig;
    private DatabaseReader databaseReader;
    
    /**
     * Initialize MaxMind database reader on service startup
     * Validates database file exists and is readable
     */
    @PostConstruct
    public void initialize() {
        try {
            File database = new File(databaseConfig.getPath());
            if (!database.exists()) {
                log.warn("GeoIP database not found at: {}. Service will work in limited mode.", 
                    databaseConfig.getPath());
                return; // Don't throw exception, just work without database
            }
            
            this.databaseReader = new DatabaseReader.Builder(database).build();
            log.info("GeoIP service initialized successfully with database: {}", 
                databaseConfig.getPath());
                
        } catch (IOException e) {
            log.error("Failed to initialize GeoIP database reader: {}", e.getMessage());
            // Don't throw exception in test environment
        }
    }
    
    /**
     * Clean up database reader resources on service shutdown
     */
    @PreDestroy
    public void cleanup() {
        if (databaseReader != null) {
            try {
                databaseReader.close();
                log.info("GeoIP database reader closed successfully");
            } catch (IOException e) {
                log.error("Error closing GeoIP database reader", e);
            }
        }
    }
    
    /**
     * Resolve geographic location from IP address string
     * 
     * @param ipAddress IP address as string (IPv4 or IPv6)
     * @return Optional containing GeoLocation if resolution successful, empty otherwise
     */
    public Optional<GeoLocation> resolveLocation(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            log.debug("Empty IP address provided for geolocation");
            return Optional.empty();
        }
        
        try {
            // Handle common edge cases for local/private IPs
            if (isPrivateOrLocalIp(ipAddress)) {
                log.debug("Private/local IP address detected: {}", ipAddress);
                return createLocalGeoLocation(ipAddress);
            }
            
            InetAddress inetAddress = InetAddress.getByName(ipAddress.trim());
            return resolveLocation(inetAddress);
            
        } catch (Exception e) {
            log.warn("Failed to resolve location for IP: {} - {}", ipAddress, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Resolve geographic location from InetAddress object
     * Core method that performs MaxMind database lookup
     * 
     * @param inetAddress InetAddress object to lookup
     * @return Optional containing GeoLocation if resolution successful, empty otherwise
     */
    public Optional<GeoLocation> resolveLocation(InetAddress inetAddress) {
        if (databaseReader == null) {
            log.error("GeoIP database reader not initialized");
            return Optional.empty();
        }
        
        try {
            CityResponse response = databaseReader.city(inetAddress);
            return Optional.of(mapToGeoLocation(inetAddress.getHostAddress(), response));
            
        } catch (GeoIp2Exception e) {
            log.debug("IP not found in GeoIP database: {} - {}", 
                inetAddress.getHostAddress(), e.getMessage());
            return Optional.empty();
            
        } catch (IOException e) {
            log.error("Error reading GeoIP database for IP: {} - {}", 
                inetAddress.getHostAddress(), e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Check if location resolution is available and database is loaded
     * 
     * @return true if service is ready to resolve locations
     */
    public boolean isServiceAvailable() {
        return databaseReader != null;
    }
    
    /**
     * Get database metadata information
     * 
     * @return metadata string with database info
     */
    public String getDatabaseInfo() {
        if (databaseReader == null) {
            return "Database not loaded";
        }
        
        try {
            var metadata = databaseReader.getMetadata();
            return String.format("MaxMind %s (Built: %s)", 
                metadata.getDatabaseType(), 
                metadata.getBuildDate());
        } catch (Exception e) {
            return "Unable to read database metadata";
        }
    }
    
    /**
     * Map MaxMind CityResponse to our GeoLocation model
     * 
     * @param ipAddress original IP address
     * @param response MaxMind city response
     * @return GeoLocation object with extracted data
     */
    private GeoLocation mapToGeoLocation(String ipAddress, CityResponse response) {
        return GeoLocation.builder()
            .ipAddress(ipAddress)
            .countryCode(response.getCountry().getIsoCode())
            .countryName(response.getCountry().getName())
            .city(response.getCity().getName())
            .region(response.getMostSpecificSubdivision().getName())
            .postalCode(response.getPostal().getCode())
            .latitude(response.getLocation().getLatitude())
            .longitude(response.getLocation().getLongitude())
            .timezone(response.getLocation().getTimeZone())
            .accuracyRadius(response.getLocation().getAccuracyRadius())
            .isProxy(response.getTraits().isAnonymousProxy() || response.getTraits().isSatelliteProvider())
            .resolvedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Check if IP address is private or local
     * 
     * @param ipAddress IP address string
     * @return true if IP is private/local
     */
    private boolean isPrivateOrLocalIp(String ipAddress) {
        return ipAddress.startsWith("127.") ||
               ipAddress.startsWith("10.") ||
               ipAddress.startsWith("192.168.") ||
               ipAddress.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*") ||
               ipAddress.equals("::1") ||
               ipAddress.startsWith("fe80:");
    }
    
    /**
     * Create a default GeoLocation for private/local IPs
     * 
     * @param ipAddress the private IP address
     * @return Optional containing local GeoLocation
     */
    private Optional<GeoLocation> createLocalGeoLocation(String ipAddress) {
        return Optional.of(GeoLocation.builder()
            .ipAddress(ipAddress)
            .countryCode("LOCAL")
            .countryName("Local Network")
            .city("Local")
            .region("Local")
            .isProxy(false)
            .resolvedAt(LocalDateTime.now())
            .build());
    }
}