package com.sprint.mission.discodeit.security;

import static org.assertj.core.api.Assertions.assertThat;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.auth.TokenPair;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtTokenProviderTest {

  private static final String SECRET = "01234567890123456789012345678901";
  private static final Instant NOW = Instant.parse("2026-09-27T00:00:00Z");

  private JwtTokenProvider tokenProvider;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    JwtProperties properties = properties(Duration.ofMinutes(30), Duration.ofDays(7));
    tokenProvider = new JwtTokenProvider(properties, Clock.fixed(NOW, ZoneOffset.UTC));
    userDetails = User.withUsername("tester")
        .password("password")
        .roles("USER")
        .build();
  }

  @Test
  void issueAccessToken() {
    String token = tokenProvider.generateAccessToken(userDetails);

    assertThat(tokenProvider.validateAccessToken(token)).isTrue();
    assertThat(tokenProvider.getClaims(token).getSubject()).isEqualTo("tester");
  }

  @Test
  void rotateTokens() {
    String oldRefreshToken = tokenProvider.generateRefreshToken(userDetails);

    TokenPair tokens = tokenProvider.rotateTokens(oldRefreshToken);

    assertThat(tokens.refreshToken()).isNotEqualTo(oldRefreshToken);
    assertThat(tokenProvider.validateAccessToken(tokens.accessToken())).isTrue();
    assertThat(tokenProvider.validateRefreshToken(tokens.refreshToken())).isTrue();
  }

  @Test
  void rejectInvalidTokens() {
    JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(
        properties(Duration.ofSeconds(1), Duration.ofSeconds(1)),
        Clock.fixed(NOW.minusSeconds(2), ZoneOffset.UTC));
    String expiredToken = expiredTokenProvider.generateAccessToken(userDetails);

    assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    assertThat(tokenProvider.validateToken("not-a-jwt")).isFalse();
  }

  private static JwtProperties properties(Duration accessExpiration, Duration refreshExpiration) {
    JwtProperties properties = new JwtProperties();
    properties.setIssuer("discodeit");
    properties.setSecret(SECRET);
    properties.setAccessTokenExpiration(accessExpiration);
    properties.setRefreshTokenExpiration(refreshExpiration);
    return properties;
  }
}
