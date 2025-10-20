package org.aibles.demo_maxmind_geolite2.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.aibles.demo_maxmind_geolite2.enums.ProxyHeader;

@UtilityClass
public class IpExtractorUtil {
    
    private static final String[] PROXY_HEADERS = ProxyHeader.getHeaderArray();
    
    public static String extractClientIp(HttpServletRequest request) {
        for (String header : PROXY_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }
    
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}