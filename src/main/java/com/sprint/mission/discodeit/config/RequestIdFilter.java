package com.sprint.mission.discodeit.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

  private static final String REQUEST_ID = "requestId";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String requestId = UUID.randomUUID().toString();

    try {
      MDC.put(REQUEST_ID, requestId);
      response.setHeader("X-Request-Id", requestId);

      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(REQUEST_ID);
    }
  }
}
