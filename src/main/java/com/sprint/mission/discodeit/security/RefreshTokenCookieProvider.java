package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieProvider {

  public static final String COOKIE_NAME = "REFRESH_TOKEN";

  private final Duration maxAge;
  private final boolean secure;
  private final String sameSite;

  public RefreshTokenCookieProvider(
      @Value("${discodeit.jwt.refresh-token-expiration}") long expirationSeconds,
      @Value("${discodeit.jwt.cookie.secure:true}") boolean secure,
      @Value("${discodeit.jwt.cookie.same-site:Lax}") String sameSite
  ) {
    if (expirationSeconds <= 0) {
      throw new IllegalArgumentException("토큰 만료 시간은 양수여야 합니다.");
    }

    if (!"Lax".equals(sameSite)
        && !"Strict".equals(sameSite)
        && !"None".equals(sameSite)) {
      throw new IllegalArgumentException("잘못된 SameSite 설정입니다.");
    }

    if ("None".equals(sameSite) && !secure) {
      throw new IllegalArgumentException("SameSite=None은 Secure가 필요합니다.");
    }

    this.maxAge = Duration.ofSeconds(expirationSeconds);
    this.secure = secure;
    this.sameSite = sameSite;
  }

  public void addCookie(HttpServletResponse response, String token) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        create(token, maxAge).toString()
    );
  }

  public void clearCookie(HttpServletResponse response) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        create("", Duration.ZERO).toString()
    );
  }

  private ResponseCookie create(String value, Duration age) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path("/")
        .maxAge(age)
        .build();
  }
}