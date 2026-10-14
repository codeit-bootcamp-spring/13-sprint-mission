package com.sprint.mission.discodeit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String accessToken = resolveAccessToken(request);

    if (accessToken != null
        && jwtTokenProvider.validateAccessToken(accessToken)
        && jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken)) {

      String email = jwtTokenProvider.getEmail(accessToken);

      CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
          email);

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          userDetails,
          null,
          userDetails.getAuthorities()
      );

      SecurityContext context = SecurityContextHolder.createEmptyContext();

      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
    }

    filterChain.doFilter(request, response);
  }

  private String resolveAccessToken(HttpServletRequest request) {

    String authorization = request.getHeader(AUTHORIZATION_HEADER);

    if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
      return null;
    }

    return authorization.substring(BEARER_PREFIX.length());
  }
}
