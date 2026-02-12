package org.aibles.demo_maxmind_geolite2.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Enumeration of HTTP headers used for proxy IP detection
 * Used for extracting real client IP addresses from HTTP requests
 */
@Getter
public enum ProxyHeader {
    
    X_FORWARDED_FOR("X-Forwarded-For"),
    X_REAL_IP("X-Real-IP"),
    PROXY_CLIENT_IP("Proxy-Client-IP"),
    WL_PROXY_CLIENT_IP("WL-Proxy-Client-IP"),
    HTTP_X_FORWARDED_FOR("HTTP_X_FORWARDED_FOR"),
    HTTP_X_FORWARDED("HTTP_X_FORWARDED"),
    HTTP_X_CLUSTER_CLIENT_IP("HTTP_X_CLUSTER_CLIENT_IP"),
    HTTP_CLIENT_IP("HTTP_CLIENT_IP"),
    HTTP_FORWARDED_FOR("HTTP_FORWARDED_FOR"),
    HTTP_FORWARDED("HTTP_FORWARDED"),
    HTTP_VIA("HTTP_VIA"),
    REMOTE_ADDR("REMOTE_ADDR"),
    X_FORWARDED("X-Forwarded"),
    FORWARDED_FOR("Forwarded-For"),
    FORWARDED("Forwarded"),
    X_CLUSTER_CLIENT_IP("X-Cluster-Client-IP"),
    CLIENT_IP("Client-IP");
    
    private final String headerName;
    
    private static final List<String> HEADER_NAMES = Arrays.stream(values())
            .map(ProxyHeader::getHeaderName)
            .collect(Collectors.toList());
    
    ProxyHeader(String headerName) {
        this.headerName = headerName;
    }

  public static List<String> getAllHeaderNames() {
        return HEADER_NAMES;
    }
    
    public static String[] getHeaderArray() {
        return HEADER_NAMES.toArray(new String[0]);
    }
    
    public static boolean isProxyHeader(String headerName) {
        return headerName != null && HEADER_NAMES.contains(headerName);
    }
}