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
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private static final String TOKEN_TYPE_CLAIM = "token_type";

    @Value("${discodeit.jwt.secret}")
    private String secret;

    @Value("${discodeit.jwt.access-token-expiration}")
    private Duration accessTokenExpiration;

    @Value("${discodeit.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(
                userDetails.getUsername(),
                TOKEN_TYPE_ACCESS,
                accessTokenExpiration
        );
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(
                userDetails.getUsername(),
                TOKEN_TYPE_REFRESH,
                refreshTokenExpiration
        );
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    public String getUsername(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getSubject();
        } catch (ParseException exception) {
            throw new IllegalArgumentException(
                    "JWT에서 사용자 이름을 추출할 수 없습니다.",
                    exception
            );
        }
    }

    private String generateToken(
            String username,
            String tokenType,
            Duration expiration
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        try {
            signedJwt.sign(
                    new MACSigner(secret.getBytes(StandardCharsets.UTF_8))
            );
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException(
                    "JWT를 발급할 수 없습니다.",
                    exception
            );
        }
    }

    private boolean validateToken(
            String token,
            String expectedTokenType
    ) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();

            boolean validSignature = signedJwt.verify(
                    new MACVerifier(secret.getBytes(StandardCharsets.UTF_8))
            );
            boolean notExpired = claims.getExpirationTime() != null
                    && claims.getExpirationTime().after(new Date());
            boolean validTokenType = expectedTokenType.equals(
                    claims.getStringClaim(TOKEN_TYPE_CLAIM)
            );

            return validSignature && notExpired && validTokenType;
        } catch (ParseException | JOSEException exception) {
            return false;
        }
    }
}