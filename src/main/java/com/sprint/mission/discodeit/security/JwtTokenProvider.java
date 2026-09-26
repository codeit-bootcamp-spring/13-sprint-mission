package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";

  private final byte[] secret;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;

  public JwtTokenProvider(
      @Value("${discodeit.jwt.secret}") String secret,
      @Value("${discodeit.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${discodeit.jwt.refresh-token-expiration}") long refreshTokenExpiration
  ) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    return generateToken(
        userDetails.getUsername(),
        ACCESS_TOKEN_TYPE,
        accessTokenExpiration
    );
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    return generateToken(
        userDetails.getUsername(),
        REFRESH_TOKEN_TYPE,
        refreshTokenExpiration
    );
  }

  public String refreshAccessToken(String refreshToken) {
    if (!validateRefreshToken(refreshToken)) {
      throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
    }

    return generateToken(
        getUsername(refreshToken),
        ACCESS_TOKEN_TYPE,
        accessTokenExpiration
    );
  }

  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);

      if (!signedJWT.verify(new MACVerifier(secret))) {
        return false;
      }

      Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

      return expirationTime != null && expirationTime.after(new Date());
    } catch (ParseException | JOSEException e) {
      return false;
    }
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token) && isAccessToken(token);
  }

  public String getUsername(String token) {
    try {
      return SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getSubject();
    } catch (ParseException e) {
      throw new IllegalArgumentException("유효하지 않은 JWT입니다.", e);
    }
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token) && isRefreshToken(token);
  }

  private boolean isRefreshToken(String token) {
    try {
      String tokenType = SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getStringClaim("type");

      return REFRESH_TOKEN_TYPE.equals(tokenType);
    } catch (ParseException e) {
      return false;
    }
  }

  private String generateToken(
      String username,
      String tokenType,
      long expirationSeconds
  ) {
    try {
      Instant now = Instant.now();

      JWTClaimsSet claims = new JWTClaimsSet.Builder()
          .subject(username)
          .issueTime(Date.from(now))
          .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
          .jwtID(UUID.randomUUID().toString())
          .claim("type", tokenType)
          .build();

      SignedJWT signedJWT = new SignedJWT(
          new JWSHeader(JWSAlgorithm.HS256),
          claims
      );

      signedJWT.sign(new MACSigner(secret));

      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 발급에 실패했습니다.", e);
    }
  }

  private boolean isAccessToken(String token) {
    try {
      String tokenType = SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getStringClaim("type");

      return ACCESS_TOKEN_TYPE.equals(tokenType);
    } catch (ParseException e) {
      return false;
    }
  }
}