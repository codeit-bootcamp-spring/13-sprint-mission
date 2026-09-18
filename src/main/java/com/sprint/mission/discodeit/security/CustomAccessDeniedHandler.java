package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {

    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        Map.of(),
        errorCode.getStatus().value(),
        accessDeniedException.getClass().getSimpleName()
    );

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(
        response.getWriter(),
        errorResponse
    );
  }
}
