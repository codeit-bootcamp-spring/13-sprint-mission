package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.response.UserDto;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

  private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;

  private static final String USER_ID_CLAIM = "userId";
  private static final String ROLE_CLAIM = "role";
  private static final String TOKEN_TYPE_CLAIM = "tokenType";

  private static final String ACCESS_TOKEN_TYPE = "access";
  private static final String REFRESH_TOKEN_TYPE = "refresh";

  private final JwtProperties properties;
  private final byte[] secretBytes;
  private final DefaultJWTProcessor<SecurityContext> jwtProcessor;

  public JwtTokenProvider(JwtProperties properties) {
    this.properties = properties;
    this.secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);

    if (secretBytes.length < 32) {
      throw new IllegalArgumentException(
          "JWT secret은 HS256 사용을 위해 32바이트 이상이어야 합니다."
      );
    }

    this.jwtProcessor = createJwtProcessor();
  }

  public String createAccessToken(UserDto userDto) {
    return createToken(
        userDto,
        properties.accessTokenExpiration(),
        ACCESS_TOKEN_TYPE
    );
  }

  public String createRefreshToken(UserDto userDto) {
    return createToken(
        userDto,
        properties.refreshTokenExpiration(),
        REFRESH_TOKEN_TYPE
    );
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token, ACCESS_TOKEN_TYPE);
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token, REFRESH_TOKEN_TYPE);
  }

  public String getEmail(String token) {
    try {
      return SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getSubject();
    } catch (ParseException e) {
      throw new IllegalArgumentException("JWT에서 사용자 정보를 읽을 수 없습니다.", e);
    }
  }

  public boolean isExpired(String token) {
    try {
      Date expirationTime = SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getExpirationTime();

      return expirationTime == null
          || !expirationTime.toInstant().isAfter(Instant.now());
    } catch (ParseException e) {
      return true;
    }
  }

  private String createToken(
      UserDto userDto,
      Duration expiration,
      String tokenType
  ) {
    Instant now = Instant.now();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(userDto.email())
        .issuer(properties.issuer())
        .claim(USER_ID_CLAIM, userDto.id().toString())
        .claim(ROLE_CLAIM, userDto.role().name())
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .issueTime(Date.from(now))
        .expirationTime(Date.from(now.plus(expiration)))
        .build();

    SignedJWT signedJwt = new SignedJWT(
        new JWSHeader.Builder(ALGORITHM)
            .type(JOSEObjectType.JWT)
            .build(),
        claims
    );

    try {
      signedJwt.sign(new MACSigner(secretBytes));
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT 발급에 실패했습니다.", e);
    }

    return signedJwt.serialize();
  }

  private boolean validateToken(String token, String expectedTokenType) {
    try {
      JWTClaimsSet claims = jwtProcessor.process(token, null);

      return expectedTokenType.equals(
          claims.getStringClaim(TOKEN_TYPE_CLAIM)
      );
    } catch (ParseException | BadJOSEException | JOSEException e) {
      return false;
    }
  }

  private DefaultJWTProcessor<SecurityContext> createJwtProcessor() {
    DefaultJWTProcessor<SecurityContext> processor =
        new DefaultJWTProcessor<>();

    processor.setJWSKeySelector(
        new JWSVerificationKeySelector<>(
            ALGORITHM,
            new ImmutableSecret<>(secretBytes)
        )
    );

    JWTClaimsSet expectedClaims = new JWTClaimsSet.Builder()
        .issuer(properties.issuer())
        .build();

    processor.setJWTClaimsSetVerifier(
        new DefaultJWTClaimsVerifier<>(
            expectedClaims,
            Set.of(
                JWTClaimNames.SUBJECT,
                JWTClaimNames.ISSUED_AT,
                JWTClaimNames.EXPIRATION_TIME,
                USER_ID_CLAIM,
                ROLE_CLAIM,
                TOKEN_TYPE_CLAIM
            )
        )
    );

    return processor;
  }

  public UUID getUserId(String token) {
    try {
      String userId = SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getStringClaim(USER_ID_CLAIM);

      return UUID.fromString(userId);
    } catch (ParseException | IllegalArgumentException e) {
      throw new IllegalArgumentException("JWT에서 사용자 ID를 읽을 수 없습니다.", e);
    }
  }

  public ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie
        .from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(properties.refreshTokenExpiration())
        .sameSite("Lax")
        .build();
  }

  public ResponseCookie createRefreshTokenDeleteCookie() {
    return ResponseCookie
        .from(REFRESH_TOKEN_COOKIE_NAME, "")
        .httpOnly(true)
        .secure(false)
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();
  }
}