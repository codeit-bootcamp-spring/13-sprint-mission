package com.sprint.mission.discodeit.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final byte[] secret;

    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValidity,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValidity
    ) {
        this.secret = secret.getBytes();
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    /**
     * Access Token 발급
     */
    public String generateAccessToken(
            UUID userId,
            String username,
            Role role
    ) {

        return generateToken(
                userId,
                username,
                role,
                ACCESS_TOKEN_TYPE,
                accessTokenValidity
        );
    }

    /**
     * Refresh Token 발급
     */
    public String generateRefreshToken(
            UUID userId,
            String username,
            Role role
    ) {

        return generateToken(
                userId,
                username,
                role,
                REFRESH_TOKEN_TYPE,
                refreshTokenValidity
        );
    }

    /**
     * JWT 생성
     */
    private String generateToken(
            UUID userId,
            String username,
            Role role,
            String tokenType,
            long validity
    ) {

        Instant now =
                Instant.now();

        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(username)
                        .claim(
                                CLAIM_USER_ID,
                                userId.toString()
                        )
                        .claim(
                                CLAIM_ROLE,
                                role.name()
                        )
                        .claim(
                                CLAIM_TOKEN_TYPE,
                                tokenType
                        )
                        .issueTime(
                                Date.from(now)
                        )
                        .expirationTime(
                                Date.from(
                                        now.plusMillis(validity)
                                )
                        )
                        .jwtID(
                                UUID.randomUUID()
                                        .toString()
                        )
                        .build();

        SignedJWT signedJWT =
                new SignedJWT(
                        new JWSHeader(
                                JWSAlgorithm.HS256
                        ),
                        claimsSet
                );

        try {

            signedJWT.sign(
                    new MACSigner(secret)
            );

            return signedJWT.serialize();

        } catch (JOSEException e) {

            throw new IllegalStateException(
                    "JWT 토큰 생성에 실패했습니다.",
                    e
            );
        }
    }

    /**
     * JWT 공통 유효성 검사
     *
     * 서명과 만료 시간을 검사한다.
     */
    public boolean validateToken(
            String token
    ) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            boolean verified =
                    signedJWT.verify(
                            new MACVerifier(secret)
                    );

            if (!verified) {
                return false;
            }

            Date expirationTime =
                    signedJWT
                            .getJWTClaimsSet()
                            .getExpirationTime();

            if (expirationTime == null) {
                return false;
            }

            return expirationTime.after(
                    new Date()
            );

        } catch (
                ParseException |
                JOSEException e
        ) {

            return false;
        }
    }

    /**
     * Access Token 유효성 검사
     *
     * 기본 JWT 검증 후 tokenType이 access인지 확인한다.
     */
    public boolean validateAccessToken(
            String token
    ) {

        if (!validateToken(token)) {
            return false;
        }

        try {

            JWTClaimsSet claimsSet =
                    getClaims(token);

            String tokenType =
                    claimsSet.getStringClaim(
                            CLAIM_TOKEN_TYPE
                    );

            return ACCESS_TOKEN_TYPE.equals(
                    tokenType
            );

        } catch (ParseException e) {

            return false;
        }
    }

    /**
     * Refresh Token 유효성 검사
     *
     * 기본 JWT 검증 후 tokenType이 refresh인지 확인한다.
     */
    public boolean validateRefreshToken(
            String token
    ) {

        if (!validateToken(token)) {
            return false;
        }

        try {

            JWTClaimsSet claimsSet =
                    getClaims(token);

            String tokenType =
                    claimsSet.getStringClaim(
                            CLAIM_TOKEN_TYPE
                    );

            return REFRESH_TOKEN_TYPE.equals(
                    tokenType
            );

        } catch (ParseException e) {

            return false;
        }
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    public String refreshAccessToken(
            String refreshToken
    ) {

        if (!validateRefreshToken(refreshToken)) {

            throw new IllegalArgumentException(
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        try {

            JWTClaimsSet claimsSet =
                    getClaims(refreshToken);

            UUID userId =
                    UUID.fromString(
                            claimsSet.getStringClaim(
                                    CLAIM_USER_ID
                            )
                    );

            String username =
                    claimsSet.getSubject();

            Role role =
                    Role.valueOf(
                            claimsSet.getStringClaim(
                                    CLAIM_ROLE
                            )
                    );

            return generateAccessToken(
                    userId,
                    username,
                    role
            );

        } catch (ParseException e) {

            throw new IllegalArgumentException(
                    "Refresh Token을 분석할 수 없습니다.",
                    e
            );
        }
    }

    /**
     * JWT Claim 조회
     */
    private JWTClaimsSet getClaims(
            String token
    ) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            return signedJWT
                    .getJWTClaimsSet();

        } catch (ParseException e) {

            throw new IllegalArgumentException(
                    "JWT 토큰을 분석할 수 없습니다.",
                    e
            );
        }
    }

    /**
     * 사용자 ID 조회
     */
    public UUID getUserId(
            String token
    ) {

        try {

            return UUID.fromString(
                    getClaims(token)
                            .getStringClaim(
                                    CLAIM_USER_ID
                            )
            );

        } catch (ParseException e) {

            throw new IllegalArgumentException(
                    "JWT에서 userId를 조회할 수 없습니다.",
                    e
            );
        }
    }

    /**
     * username 조회
     */
    public String getUsername(
            String token
    ) {

        return getClaims(token)
                .getSubject();
    }

    /**
     * Role 조회
     */
    public Role getRole(
            String token
    ) {

        try {

            String role =
                    getClaims(token)
                            .getStringClaim(
                                    CLAIM_ROLE
                            );

            return Role.valueOf(
                    role
            );

        } catch (ParseException e) {

            throw new IllegalArgumentException(
                    "JWT에서 Role을 조회할 수 없습니다.",
                    e
            );
        }
    }
}