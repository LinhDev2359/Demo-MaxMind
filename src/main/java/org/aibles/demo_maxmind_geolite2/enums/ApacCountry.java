package org.aibles.demo_maxmind_geolite2.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Enumeration of supported Asia-Pacific countries
 * Used for regional security assessment and compliance
 */
@Getter
public enum ApacCountry {
    
    VN("VN", "Vietnam"),
    SG("SG", "Singapore"),
    JP("JP", "Japan"),
    TH("TH", "Thailand"),
    MY("MY", "Malaysia"),
    ID("ID", "Indonesia"),
    PH("PH", "Philippines"),
    KR("KR", "South Korea"),
    TW("TW", "Taiwan"),
    HK("HK", "Hong Kong"),
    IN("IN", "India"),
    AU("AU", "Australia"),
    NZ("NZ", "New Zealand");
    
    private final String countryCode;
    private final String countryName;
    
    private static final Set<String> COUNTRY_CODES = Arrays.stream(values())
            .map(ApacCountry::getCountryCode)
            .collect(Collectors.toSet());
    
    ApacCountry(String countryCode, String countryName) {
        this.countryCode = countryCode;
        this.countryName = countryName;
    }

  public static boolean isApacCountry(String countryCode) {
        return countryCode != null && COUNTRY_CODES.contains(countryCode.toUpperCase());
    }
    
    public static ApacCountry fromCode(String countryCode) {
        if (countryCode == null) {
            return null;
        }
        
        for (ApacCountry country : values()) {
            if (country.countryCode.equalsIgnoreCase(countryCode)) {
                return country;
            }
        }
        return null;
    }
    
    public static Set<String> getAllCountryCodes() {
        return COUNTRY_CODES;
    }
}