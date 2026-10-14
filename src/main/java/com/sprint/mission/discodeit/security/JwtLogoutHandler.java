package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @CacheEvict(cacheNames = "users", allEntries = true)
  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) {

    ResponseCookie deletedCookie = jwtTokenProvider.createRefreshTokenDeleteCookie();

    response.addHeader(
        HttpHeaders.SET_COOKIE, deletedCookie.toString()
    );

    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return;
    }

    Arrays.stream(cookies)
        .filter(cookie -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(jwtRegistry::hasActiveJwtInformationByRefreshToken)
        .findFirst()
        .ifPresent(refreshToken -> {
          jwtRegistry.invalidateJwtInformationByUserId(jwtTokenProvider.getUserId(refreshToken));
        });

  }
}
