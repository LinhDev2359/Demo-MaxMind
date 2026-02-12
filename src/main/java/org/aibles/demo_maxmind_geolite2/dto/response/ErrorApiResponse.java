package org.aibles.demo_maxmind_geolite2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorApiResponse<T> extends ApiResponse<T>{
  public ErrorApiResponse(String message) {
    super(false, message, null);
  }

  @Override
  @com.fasterxml.jackson.annotation.JsonIgnore
  public T getData() {
    return null;
  }
}
