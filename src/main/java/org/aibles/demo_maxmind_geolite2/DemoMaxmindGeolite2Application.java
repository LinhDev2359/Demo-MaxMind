package org.aibles.demo_maxmind_geolite2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for MaxMind GeoLite2 Demo
 * Provides GeoIP services for multi-country QR login tracking
 * 
 * Features:
 * - IP-based geolocation using MaxMind GeoLite2 database
 * - Automated database updates with scheduling
 * - Audit logging for QR authentication events
 * - REST API for integration with authentication systems
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class DemoMaxmindGeolite2Application {

	public static void main(String[] args) {
		SpringApplication.run(DemoMaxmindGeolite2Application.class, args);
	}

}
