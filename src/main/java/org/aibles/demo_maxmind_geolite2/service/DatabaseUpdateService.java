package org.aibles.demo_maxmind_geolite2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aibles.demo_maxmind_geolite2.config.GeoIpDatabaseConfig;
import org.aibles.demo_maxmind_geolite2.config.GeoIpUpdateConfig;
import org.aibles.demo_maxmind_geolite2.config.GeoIpServiceConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * Service for automatically updating MaxMind GeoLite2 database
 * Handles scheduled downloads, extraction, and atomic replacement of database files
 * Ensures zero-downtime updates for continuous geolocation service availability
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseUpdateService {
    
    private final GeoIpDatabaseConfig databaseConfig;
    private final GeoIpUpdateConfig updateConfig;
    private final GeoIpServiceConfig serviceConfig;
    private final GeoIpService geoIpService;
    
    private LocalDateTime lastUpdateCheck;
    private LocalDateTime lastSuccessfulUpdate;
    private volatile boolean updateInProgress = false;
    
    /**
     * Check for database updates on service startup if configured
     */
    @PostConstruct
    public void initializeUpdateCheck() {
        if (updateConfig.isCheckOnStartup()) {
            log.info("Checking for GeoIP database updates on startup...");
            CompletableFuture.runAsync(this::checkAndUpdateDatabase);
        }
    }
    
    /**
     * Scheduled task to check for database updates
     * Runs based on configured interval (default: weekly)
     */
    @Scheduled(fixedDelayString = "${geoip.update.interval-millis}")
    public void scheduledDatabaseUpdate() {
        if (!updateConfig.isEnabled()) {
            log.debug("Database updates are disabled");
            return;
        }
        
        log.info("Starting scheduled GeoIP database update check");
        checkAndUpdateDatabase();
    }
    
    /**
     * Check if database update is needed and perform update if required
     * Implements retry logic and error handling for robust updates
     */
    public void checkAndUpdateDatabase() {
        if (updateInProgress) {
            log.info("Database update already in progress, skipping");
            return;
        }
        
        if (serviceConfig.getMaxmindLicenseKey() == null || 
            serviceConfig.getMaxmindLicenseKey().equals("your-license-key-here")) {
            log.warn("MaxMind license key not configured, skipping database update");
            return;
        }
        
        updateInProgress = true;
        lastUpdateCheck = LocalDateTime.now();
        
        try {
            boolean updateNeeded = isDatabaseUpdateNeeded();
            
            if (updateNeeded) {
                log.info("Database update required, starting download...");
                boolean success = downloadAndUpdateDatabase();
                
                if (success) {
                    lastSuccessfulUpdate = LocalDateTime.now();
                    log.info("Database update completed successfully at {}", lastSuccessfulUpdate);
                } else {
                    log.error("Database update failed");
                }
            } else {
                log.info("Database is up to date, no update needed");
            }
            
        } catch (Exception e) {
            log.error("Error during database update check", e);
        } finally {
            updateInProgress = false;
        }
    }
    
    /**
     * Download and update GeoLite2 database with retry logic
     * 
     * @return true if update was successful, false otherwise
     */
    private boolean downloadAndUpdateDatabase() {
        int attempts = 0;
        int maxAttempts = updateConfig.getRetryAttempts();
        
        while (attempts < maxAttempts) {
            attempts++;
            
            try {
                log.info("Attempting database download (attempt {}/{})", attempts, maxAttempts);
                
                // Download database file
                Path tempFile = downloadDatabase();
                
                // Extract and replace database
                boolean extracted = extractAndReplaceDatabase(tempFile);
                
                if (extracted) {
                    // Clean up temp file
                    Files.deleteIfExists(tempFile);
                    
                    // Reinitialize GeoIP service with new database
                    reinitializeGeoIpService();
                    
                    return true;
                } else {
                    log.warn("Failed to extract database on attempt {}", attempts);
                }
                
            } catch (Exception e) {
                log.warn("Database download attempt {} failed: {}", attempts, e.getMessage());
                
                if (attempts >= maxAttempts) {
                    log.error("All database download attempts failed", e);
                }
            }
            
            // Wait before retry (exponential backoff)
            if (attempts < maxAttempts) {
                try {
                    long waitTime = 1000L * attempts * attempts; // 1s, 4s, 9s...
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Download GeoLite2 database from MaxMind
     * 
     * @return Path to downloaded temporary file
     * @throws Exception if download fails
     */
    private Path downloadDatabase() throws Exception {
        String downloadUrl = buildDownloadUrl();
        Path tempFile = Files.createTempFile("geolite2-", ".tar.gz");
        
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(updateConfig.getTimeout())
            .build();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(downloadUrl))
            .timeout(updateConfig.getTimeout())
            .GET()
            .build();
        
        log.info("Downloading GeoLite2 database from MaxMind...");
        
        HttpResponse<Path> response = client.send(request, 
            HttpResponse.BodyHandlers.ofFile(tempFile));
        
        if (response.statusCode() == 200) {
            log.info("Database downloaded successfully: {} bytes", Files.size(tempFile));
            return tempFile;
        } else {
            throw new RuntimeException("Failed to download database: HTTP " + response.statusCode());
        }
    }
    
    /**
     * Extract tar.gz file and replace current database
     * 
     * @param tarGzFile Path to downloaded tar.gz file
     * @return true if extraction and replacement successful
     */
    private boolean extractAndReplaceDatabase(Path tarGzFile) {
        try {
            Path databasePath = Paths.get(databaseConfig.getPath());
            Path backupPath = Paths.get(databasePath + ".backup");
            Path tempDbPath = Files.createTempFile("geolite2-db-", ".mmdb");
            
            // Extract .mmdb file from tar.gz
            try (FileInputStream fis = new FileInputStream(tarGzFile.toFile());
                 GZIPInputStream gis = new GZIPInputStream(fis);
                 TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
                
                TarArchiveEntry entry;
                while ((entry = tis.getNextTarEntry()) != null) {
                    if (entry.getName().endsWith(".mmdb")) {
                        log.info("Extracting database file: {}", entry.getName());
                        
                        try (FileOutputStream fos = new FileOutputStream(tempDbPath.toFile())) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = tis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                        break;
                    }
                }
            }
            
            // Backup current database if it exists
            if (Files.exists(databasePath)) {
                Files.copy(databasePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Current database backed up to: {}", backupPath);
            }
            
            // Replace database atomically
            Files.move(tempDbPath, databasePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Database updated successfully: {}", databasePath);
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to extract and replace database", e);
            return false;
        }
    }
    
    /**
     * Build download URL for MaxMind GeoLite2 database
     * 
     * @return Complete download URL with license key
     */
    private String buildDownloadUrl() {
        return String.format("%s?edition_id=%s&license_key=%s&suffix=%s",
            databaseConfig.getDownloadUrl(),
            databaseConfig.getEditionId(),
            serviceConfig.getMaxmindLicenseKey(),
            databaseConfig.getSuffix());
    }
    
    /**
     * Check if database update is needed based on file age
     * 
     * @return true if update is needed
     */
    private boolean isDatabaseUpdateNeeded() {
        try {
            Path databasePath = Paths.get(databaseConfig.getPath());
            
            if (!Files.exists(databasePath)) {
                log.info("Database file not found, update needed");
                return true;
            }
            
            LocalDateTime fileModified = LocalDateTime.ofInstant(
                Files.getLastModifiedTime(databasePath).toInstant(),
                java.time.ZoneOffset.UTC);
            
            LocalDateTime updateThreshold = LocalDateTime.now()
                .minus(updateConfig.getInterval());
            
            boolean updateNeeded = fileModified.isBefore(updateThreshold);
            
            log.info("Database last modified: {}, update threshold: {}, update needed: {}",
                fileModified, updateThreshold, updateNeeded);
            
            return updateNeeded;
            
        } catch (IOException e) {
            log.warn("Error checking database file status", e);
            return false;
        }
    }
    
    /**
     * Reinitialize GeoIP service after database update
     */
    private void reinitializeGeoIpService() {
        try {
            log.info("Reinitializing GeoIP service with updated database...");
            geoIpService.cleanup();
            geoIpService.initialize();
            log.info("GeoIP service reinitialized successfully");
        } catch (Exception e) {
            log.error("Failed to reinitialize GeoIP service", e);
        }
    }
    
    /**
     * Get update status information
     * 
     * @return Status string with last update times
     */
    public String getUpdateStatus() {
        return String.format("Last check: %s, Last update: %s, Update in progress: %s",
            lastUpdateCheck != null ? lastUpdateCheck.toString() : "Never",
            lastSuccessfulUpdate != null ? lastSuccessfulUpdate.toString() : "Never",
            updateInProgress);
    }
    
    /**
     * Force immediate database update (for admin/testing purposes)
     * 
     * @return true if update was successful
     */
    public boolean forceUpdate() {
        log.info("Force update requested");
        checkAndUpdateDatabase();
        return lastSuccessfulUpdate != null && 
               lastSuccessfulUpdate.isAfter(LocalDateTime.now().minusMinutes(5));
    }
}