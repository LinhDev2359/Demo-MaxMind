package org.aibles.demo_maxmind_geolite2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic API response wrapper
 * Simplified structure with only success, message, and data fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    
    private T data;
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ErrorApiResponse<>(message);
    }
}