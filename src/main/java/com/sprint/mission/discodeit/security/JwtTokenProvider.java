package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.InvalidJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String CLAIM_ROLE = "role";

    private final String secret;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${discodeit.jwt.secret}") String secret,
            @Value("${discodeit.jwt.access-token-validity}") Duration accessTokenValidity,
            @Value("${discodeit.jwt.refresh-token-validity}") Duration refreshTokenValidity,
            @Value("${discodeit.jwt.issuer}") String issuer) {
        this.secret = secret;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.issuer = issuer;
    }

    public String createAccessToken(String username, Role role) {
        return createToken(username, role, accessTokenValidity);
    }
    public String createRefreshToken(String username, Role role) {
        return createToken(username, role, refreshTokenValidity);
    }

    private String createToken(String username, Role role, Duration validity) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim(CLAIM_ROLE, role.name())
                    .issuer(issuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(validity)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));

            return jwt.serialize();
        } catch (JOSEException e) {
            throw new InvalidJwtException();
        }
    }

    // JWT를 검증하고 Claims를 반환한다.
    public JWTClaimsSet parseClaims(String token) {
        try {
            var processor = new DefaultJWTProcessor<SecurityContext>();

            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                    JWSAlgorithm.HS256, new ImmutableSecret<>(secret.getBytes(StandardCharsets.UTF_8)))
            );

            return processor.process(token, null);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new InvalidJwtException();
        }
    }

    // 유효 여부만 빠르게 판단하는 편의 메서드 (예외 → false)
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (InvalidJwtException e) {
            log.warn("[JWT] 유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }

    // 검증을 통과한 토큰에서 사용자 이름(sub) 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 검증을 통과한 토큰에서 역할(role 클레임) 추출.
    public Role getRole(String token) {
        return getRole(parseClaims(token));
    }
    // 이미 검증된 Claims에서 역할 추출
    public Role getRole(JWTClaimsSet claims) {
        try {
            return Role.valueOf(claims.getStringClaim(CLAIM_ROLE));
        } catch (ParseException | IllegalArgumentException e) {
            throw new InvalidJwtException();
        }
    }

}
