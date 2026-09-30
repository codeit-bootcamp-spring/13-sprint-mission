package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final String issuer;
    private final byte[] secret;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtTokenProvider(
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-seconds}") long accessTokenValiditySeconds,
            @Value("${jwt.refresh-token-validity-seconds}") long refreshTokenValiditySeconds
    ) {
        this.issuer = issuer;
        this.secret = secret.getBytes();
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public String generateAccessToken(DiscodeitUserDetails userDetails) {
        return generateToken(
                userDetails,
                accessTokenValiditySeconds,
                ACCESS_TOKEN_TYPE
        );
    }

    public String generateRefreshToken(DiscodeitUserDetails userDetails) {
        return generateToken(
                userDetails,
                refreshTokenValiditySeconds,
                REFRESH_TOKEN_TYPE
        );
    }

    private String generateToken(
            DiscodeitUserDetails userDetails,
            long validitySeconds,
            String tokenType
    ) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(validitySeconds);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId().toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiration))
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        try {
            signedJWT.sign(new MACSigner(secret));
        } catch (JOSEException e) {
            throw new IllegalStateException("JWT 토큰 생성에 실패했습니다.", e);
        }

        return signedJWT.serialize();
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            boolean signatureValid =
                    signedJWT.verify(new MACVerifier(secret));

            if (!signatureValid) {
                return false;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!issuer.equals(claims.getIssuer())) {
                return false;
            }

            Date expirationTime = claims.getExpirationTime();

            return expirationTime != null
                    && expirationTime.after(new Date());

        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public UUID getUserId(String token) {
        Object userId = getClaims(token).getClaim("userId");

        if (userId == null) {
            throw new IllegalArgumentException(
                    "JWT에 userId가 존재하지 않습니다."
            );
        }

        return UUID.fromString(userId.toString());
    }

    public Date getExpiration(String token) {
        return getClaims(token).getExpirationTime();
    }

    private String getTokenType(String token) {
        Object tokenType =
                getClaims(token).getClaim(TOKEN_TYPE_CLAIM);

        return tokenType == null
                ? null
                : tokenType.toString();
    }

    private JWTClaimsSet getClaims(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "유효하지 않은 JWT 형식입니다.",
                    e
            );
        }
    }
}