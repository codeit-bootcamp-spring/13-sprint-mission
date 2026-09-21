package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
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
    private static final String CLAIM_USER_ID = "user_id";
    private static final String CLAIM_TOKEN_TYPE = "token_type";

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey secretKey;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;

    public JwtTokenProvider(JwtProperties jwtProperties, Clock clock, DiscodeitUserDetailsService discodeitUserDetailsService) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
        this.discodeitUserDetailsService = discodeitUserDetailsService;
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

    // 갱신
    public TokenDto renewGenerateToken(String token) {
        return validateToken(token)
                .filter(this::isTokenRefreshType)
                .map(claims -> {
                    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) discodeitUserDetailsService.loadUserByUsername(claims.getSubject());
                    return new TokenDto(
                            generateAccessToken(
                            getUserId(claims),
                            userDetails.getUsername(),
                            userDetails.getUserDto().role()),
                            generateRefreshToken(getUserId(claims), userDetails.getUsername())
                            );
                })
                .orElseThrow(TokenRenewalFailedException::new);
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

    private Date generateExpirationDate(Instant now, Duration expirationDuration) {
        return Date.from(now.plus(expirationDuration));
    }

    private byte[] generateSecretKeyBytes() {
        return jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
    }

    private boolean isTokenRefreshType(Claims claims) {
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);

        return TokenType.REFRESH.name().equals(tokenType);
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
}
