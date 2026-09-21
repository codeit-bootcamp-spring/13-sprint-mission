package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USER_ID = "user_id";
    public static final String CLAIM_TOKEN_TYPE = "token_type";

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.secretKey = Keys.hmacShaKeyFor(generateSecretKeyBytes());
    }

    public enum TokenType {
        ACCESS, REFRESH
    }

    // 생성
    private String generateToken(UUID userId, String username, Role role, TokenType tokenType) {
        Instant now = clock.instant();

        return switch (tokenType) {
            case ACCESS ->  createAccessToken(userId, username, role, now);
            case REFRESH -> createRefreshToken(userId, username, now);
        };
    }

    // 유효성 검사
    public Optional<Claims> validateToken(String token) {
        try {
            return Optional.of(parseClaims(token));
        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            log.warn("[JWT] 유효하지 않은 토큰: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public String generateAccessToken(UUID userId, String username, Role role){
        return generateToken(userId, username, role, TokenType.ACCESS);
    }

    public String generateRefreshToken(UUID userId, String username){
        return generateToken(userId, username, null, TokenType.REFRESH);
    }

    public Claims parseClaims(String token){
        Claims payload = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String tokenType = payload.get(CLAIM_TOKEN_TYPE, String.class);
            TokenType.valueOf(tokenType);

        return payload;
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.get(CLAIM_USER_ID, String.class));
    }

    public String getTokenType(Claims claims) {
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    private Date generateExpirationDate(Instant now, Duration expirationDuration) {
        return Date.from(now.plus(expirationDuration));
    }

    private byte[] generateSecretKeyBytes() {
        return jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
    }

    private String createRefreshToken(UUID userId,String username,Instant now) {

        return Jwts.builder()
                .subject(username)
                .claims(Map.of(CLAIM_USER_ID, userId, CLAIM_TOKEN_TYPE, TokenType.REFRESH))
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(generateExpirationDate(now, jwtProperties.refreshTokenValidity()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private String createAccessToken(UUID userId, String username, Role role, Instant now) {
        return Jwts.builder()
                .subject(username)
                .claims(Map.of(CLAIM_ROLE, role.name(), CLAIM_USER_ID, userId, CLAIM_TOKEN_TYPE, TokenType.ACCESS))
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(now))
                .expiration(generateExpirationDate(now, jwtProperties.accessTokenValidity()))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public long getRefreshTokenLifetime(){
       return jwtProperties.refreshTokenValidity().getSeconds();
    }
}
