package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.auth.TokenPair;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtTokenProvider {

  private static final JWSAlgorithm SIGNATURE_ALGORITHM = JWSAlgorithm.HS256;
  private static final String TOKEN_TYPE_CLAIM = "token_type";
  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";
  private static final String USER_ID_CLAIM = "user_id";
  private static final String EMAIL_CLAIM = "email";
  private static final String ROLE_CLAIM = "role";

  private final String issuer;
  private final Duration accessTokenExpiration;
  private final Duration refreshTokenExpiration;
  private final byte[] secret;
  private final Clock clock;

  @Autowired
  public JwtTokenProvider(JwtProperties properties) {
    this(properties, Clock.systemUTC());
  }

  JwtTokenProvider(JwtProperties properties, Clock clock) {
    this.issuer = requireText(properties.getIssuer(), "JWT issuer");
    this.accessTokenExpiration = requirePositive(
        properties.getAccessTokenExpiration(), "access token expiration");
    this.refreshTokenExpiration = requirePositive(
        properties.getRefreshTokenExpiration(), "refresh token expiration");
    this.secret = requireSecret(properties.getSecret());
    this.clock = clock;
  }

  public String generateAccessToken(UserDetails userDetails) {
    return generateToken(userDetails, ACCESS_TOKEN_TYPE, accessTokenExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return generateToken(userDetails, REFRESH_TOKEN_TYPE, refreshTokenExpiration);
  }

  /**
   * 유효한 리프레시 토큰의 사용자 클레임을 그대로 사용해 새 액세스 토큰을 발급한다.
   */
  public String refreshAccessToken(String refreshToken) {
    return renewToken(requireRefreshToken(refreshToken), ACCESS_TOKEN_TYPE,
        accessTokenExpiration);
  }

  /**
   * 리프레시 토큰 Rotation을 적용해 새로운 액세스·리프레시 토큰 쌍을 발급한다.
   */
  public TokenPair rotateTokens(String refreshToken) {
    JWTClaimsSet claims = requireRefreshToken(refreshToken);
    return new TokenPair(
        renewToken(claims, ACCESS_TOKEN_TYPE, accessTokenExpiration),
        renewToken(claims, REFRESH_TOKEN_TYPE, refreshTokenExpiration)
    );
  }

  /**
   * 토큰의 형식, 알고리즘, 서명, 발급자와 유효 기간을 모두 검사한다.
   */
  public boolean validateToken(String token) {
    try {
      parseAndValidate(token);
      return true;
    } catch (IllegalArgumentException exception) {
      log.debug("유효하지 않은 JWT입니다: {}", exception.getMessage());
      return false;
    }
  }

  /**
   * 서명과 표준 클레임뿐 아니라 액세스 토큰인지도 검사한다.
   */
  public boolean validateAccessToken(String token) {
    try {
      JWTClaimsSet claims = parseAndValidate(token);
      return ACCESS_TOKEN_TYPE.equals(getStringClaim(claims, TOKEN_TYPE_CLAIM));
    } catch (IllegalArgumentException exception) {
      log.debug("유효하지 않은 액세스 토큰입니다: {}", exception.getMessage());
      return false;
    }
  }

  public boolean validateRefreshToken(String token) {
    try {
      JWTClaimsSet claims = parseAndValidate(token);
      return REFRESH_TOKEN_TYPE.equals(getStringClaim(claims, TOKEN_TYPE_CLAIM));
    } catch (IllegalArgumentException exception) {
      log.debug("유효하지 않은 리프레시 토큰입니다: {}", exception.getMessage());
      return false;
    }
  }

  public UUID getUserId(String token) {
    String userId = getStringClaim(parseAndValidate(token), USER_ID_CLAIM);
    try {
      return UUID.fromString(userId);
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("JWT 사용자 ID가 올바르지 않습니다.", exception);
    }
  }

  /**
   * 검증을 마친 토큰의 클레임을 반환한다.
   */
  public JWTClaimsSet getClaims(String token) {
    return parseAndValidate(token);
  }

  private JWTClaimsSet requireRefreshToken(String refreshToken) {
    JWTClaimsSet claims = parseAndValidate(refreshToken);
    if (!REFRESH_TOKEN_TYPE.equals(getStringClaim(claims, TOKEN_TYPE_CLAIM))) {
      throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
    }
    return claims;
  }

  private String renewToken(JWTClaimsSet claims, String tokenType, Duration expiration) {
    return sign(newClaims(claims.getSubject(), tokenType, expiration)
        .claim(USER_ID_CLAIM, getStringClaim(claims, USER_ID_CLAIM))
        .claim(EMAIL_CLAIM, getStringClaim(claims, EMAIL_CLAIM))
        .claim(ROLE_CLAIM, getStringClaim(claims, ROLE_CLAIM))
        .build());
  }

  private String generateToken(UserDetails userDetails, String tokenType, Duration expiration) {
    if (userDetails == null) {
      throw new IllegalArgumentException("사용자 정보는 필수입니다.");
    }

    JWTClaimsSet.Builder claims = newClaims(userDetails.getUsername(), tokenType, expiration);
    if (userDetails instanceof DiscodeitUserDetails discodeitUserDetails) {
      UserResponse user = discodeitUserDetails.getUserDto();
      claims.claim(USER_ID_CLAIM, user.id().toString())
          .claim(EMAIL_CLAIM, user.email())
          .claim(ROLE_CLAIM, user.role().name());
    } else {
      String role = userDetails.getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .findFirst()
          .map(authority -> authority.replaceFirst("^ROLE_", ""))
          .orElse(null);
      claims.claim(ROLE_CLAIM, role);
    }
    return sign(claims.build());
  }

  private JWTClaimsSet.Builder newClaims(String subject, String tokenType, Duration expiration) {
    Instant issuedAt = clock.instant();
    return new JWTClaimsSet.Builder()
        .issuer(issuer)
        .subject(requireText(subject, "JWT subject"))
        .jwtID(UUID.randomUUID().toString())
        .issueTime(Date.from(issuedAt))
        .expirationTime(Date.from(issuedAt.plus(expiration)))
        .claim(TOKEN_TYPE_CLAIM, tokenType);
  }

  private String sign(JWTClaimsSet claims) {
    SignedJWT signedJwt = new SignedJWT(new JWSHeader(SIGNATURE_ALGORITHM), claims);
    try {
      signedJwt.sign(new MACSigner(secret));
      return signedJwt.serialize();
    } catch (JOSEException exception) {
      throw new IllegalStateException("JWT 서명에 실패했습니다.", exception);
    }
  }

  private JWTClaimsSet parseAndValidate(String token) {
    if (!StringUtils.hasText(token)) {
      throw new IllegalArgumentException("JWT는 비어 있을 수 없습니다.");
    }

    try {
      SignedJWT signedJwt = SignedJWT.parse(token);
      if (!SIGNATURE_ALGORITHM.equals(signedJwt.getHeader().getAlgorithm())) {
        throw new IllegalArgumentException("지원하지 않는 JWT 서명 알고리즘입니다.");
      }
      if (!signedJwt.verify(new MACVerifier(secret))) {
        throw new IllegalArgumentException("JWT 서명이 올바르지 않습니다.");
      }

      JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
      validateClaims(claims);
      return claims;
    } catch (ParseException | JOSEException exception) {
      throw new IllegalArgumentException("JWT를 검증할 수 없습니다.", exception);
    }
  }

  private void validateClaims(JWTClaimsSet claims) {
    Instant now = clock.instant();
    if (!issuer.equals(claims.getIssuer())) {
      throw new IllegalArgumentException("JWT 발급자가 올바르지 않습니다.");
    }
    if (claims.getIssueTime() == null || claims.getIssueTime().toInstant().isAfter(now)) {
      throw new IllegalArgumentException("JWT 발급 시간이 올바르지 않습니다.");
    }
    if (claims.getExpirationTime() == null || !claims.getExpirationTime().toInstant().isAfter(now)) {
      throw new IllegalArgumentException("JWT가 만료되었습니다.");
    }
    if (!StringUtils.hasText(claims.getSubject())) {
      throw new IllegalArgumentException("JWT 주체가 없습니다.");
    }
    String tokenType = getStringClaim(claims, TOKEN_TYPE_CLAIM);
    if (!ACCESS_TOKEN_TYPE.equals(tokenType) && !REFRESH_TOKEN_TYPE.equals(tokenType)) {
      throw new IllegalArgumentException("JWT 종류가 올바르지 않습니다.");
    }
  }

  private static String getStringClaim(JWTClaimsSet claims, String claimName) {
    try {
      return claims.getStringClaim(claimName);
    } catch (ParseException exception) {
      throw new IllegalArgumentException("JWT 클레임 형식이 올바르지 않습니다: " + claimName,
          exception);
    }
  }

  private static byte[] requireSecret(String secret) {
    String value = requireText(secret, "JWT secret");
    byte[] secretBytes = value.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret은 32바이트 이상이어야 합니다.");
    }
    return secretBytes;
  }

  private static Duration requirePositive(Duration duration, String fieldName) {
    if (duration == null || duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException(fieldName + "은 0보다 커야 합니다.");
    }
    return duration;
  }

  private static String requireText(String value, String fieldName) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalArgumentException(fieldName + "은 비어 있을 수 없습니다.");
    }
    return value;
  }

}
