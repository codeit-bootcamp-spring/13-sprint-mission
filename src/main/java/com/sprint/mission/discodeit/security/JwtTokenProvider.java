package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TOKEN_TYPE = "type";

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties properties;
    private final Clock clock;
    private final byte[] secret;

    public JwtTokenProvider(
            JwtProperties properties,
            Clock clock
    ) {
        this.properties = properties;
        this.clock = clock;

        if (properties.getSecretKey() == null
                || properties.getSecretKey().isBlank()) {
            throw new IllegalArgumentException(
                    "JWT 비밀키는 필수입니다."
            );
        }

        this.secret = properties.getSecretKey()
                .getBytes(StandardCharsets.UTF_8);

        if (secret.length < 32) {
            throw new IllegalArgumentException(
                    "JWT 비밀키는 32바이트 이상이어야 합니다."
            );
        }
    }

    public String createAccessToken(UUID userId, String username, Role role) {

        Objects.requireNonNull(
                role,
                "Access Token의 사용자 권한은 필수입니다."
        );

        return  createToken(
                userId,
                username,
                role,
                ACCESS_TOKEN_TYPE,
                properties.getAccessTokenValidity()
        );
    }

    public String createRefreshToken(UUID userId, String username) {
        return  createToken(
                userId,
                username,
                null,
                REFRESH_TOKEN_TYPE,
                properties.getRefreshTokenValidity()
        );
    }

    public String refreshAccessToken(String refreshToken, Role currentRole) {
        JWTClaimsSet claims = parseClaims(refreshToken);

        validateTokenType(claims, REFRESH_TOKEN_TYPE);

        UUID userId = getUserId(claims);
        String username = getUsername(claims);

        return createAccessToken(
                userId,
                username,
                currentRole
        );
    }

    private String createToken (
            UUID userId,
            String username,
            Role role,
            String tokenType,
            Duration validity
    ) {
        Objects.requireNonNull(userId, "사용자의 ID는 필수입니다.");
        Objects.requireNonNull(username, "사용자 이름은 필수입니다.");
        Objects.requireNonNull(validity, "토큰 유효시간은 필수입니다.");
        if (username.isBlank()) {
            throw new IllegalArgumentException(
                    "사용자 이름은 비어 있을 수 없습니다."
            );
        }

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(validity);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(properties.getIssuer())
                .subject(username)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_TOKEN_TYPE, tokenType);

        if (role != null) {
            claimsBuilder.claim(CLAIM_ROLE, role.name());
        }

        JWTClaimsSet claims = claimsBuilder.build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256)
                        .type(JOSEObjectType.JWT)
                        .build(), claims);

        try {
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(
                    "JWT 발급에 실패했습니다.",
                    e
            );
        }
    }

    public JWTClaimsSet parseClaims(String token) {

        if(token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT는 필수입니다.");
        }

        try {
            SignedJWT signedJwt = SignedJWT.parse(token);

            if (!JWSAlgorithm.HS256.equals(
                    signedJwt.getHeader().getAlgorithm()
            )) {
                throw new IllegalArgumentException(
                        "지원하지 않는 JWT 알고리즘입니다."
                );
            }

            boolean signatureVerified = signedJwt.verify((new MACVerifier(secret)));

            if (!signatureVerified) {
                throw new IllegalArgumentException("JWT 서명이 유효하지 않습니다.");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            if (!properties.getIssuer().equals(claims.getIssuer())) {
                throw new IllegalArgumentException("JWT 발급자가 일치하지 않습니다.");
            }

            Date expirationTime =
                    claims.getExpirationTime();
            if (expirationTime == null
                    || !expirationTime.toInstant().isAfter(clock.instant())) {
                throw new IllegalArgumentException("JWT가 만료되었습니다.");
            }

            return claims;
        } catch (ParseException | JOSEException e) {
            throw new IllegalArgumentException("JWT 검증할 수 없습니다.", e);
        }
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn(
                    "[JWT] 유효하지 않은 토큰: {}",
                    e.getMessage()
            );
            return false;
        }
    }

    public UUID getUserId(String token) {
        return getUserId(parseClaims(token));
    }

    public UUID getUserId(JWTClaimsSet claims) {
        Object userId =
                claims.getClaim(CLAIM_USER_ID);

        if (!(userId instanceof String value)) {
            throw new IllegalArgumentException(
                    "JWT에 사용자 ID가 없습니다."
            );
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "JWT 사용자 ID 형식이 올바르지 않습니다.",
                    e
            );
        }
    }

    private void validateTokenType(JWTClaimsSet claims, String expectedTokenType) {
        Object tokenType = claims.getClaim((CLAIM_TOKEN_TYPE));

        if(!expectedTokenType.equals(tokenType)) {
            throw new IllegalArgumentException("올바른 토큰의 종류가 아닙니다.");
        }
    }

    public long getAccessTokenExpirationTime() {
        return properties.getAccessTokenValidity().toSeconds();
    }

    public long getRefreshTokenExpirationTime() {
        return properties.getRefreshTokenValidity().toSeconds();
    }

    public JWTClaimsSet parseAccessToken(String token) {
        JWTClaimsSet claims = parseClaims(token);
        validateTokenType(claims, ACCESS_TOKEN_TYPE);
        return claims;
    }

    public String getUsername(JWTClaimsSet claims) {
        String username = claims.getSubject();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT에 사용자 이름이 없습니다."
            );
        }

        return username;
    }

    public JWTClaimsSet parseRefreshToken(String token) {
        JWTClaimsSet claims = parseClaims(token);
        validateTokenType(claims, REFRESH_TOKEN_TYPE);
        return claims;
    }
}
