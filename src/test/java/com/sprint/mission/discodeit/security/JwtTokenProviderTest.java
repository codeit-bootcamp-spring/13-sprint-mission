package com.sprint.mission.discodeit.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "12345678901234567890123456789012";

    private static final Duration ACCESS_TOKEN_VALIDITY =
            Duration.ofMinutes(10);

    private static final Duration REFRESH_TOKEN_VALIDITY =
            Duration.ofDays(14);

    private static final String ISSUER =
            "discodeit";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                SECRET,
                ACCESS_TOKEN_VALIDITY,
                REFRESH_TOKEN_VALIDITY,
                ISSUER
        );
    }

    @Nested
    @DisplayName("Access Token")
    class AccessToken {

        @Test
        @DisplayName("Access Token을 생성하고 사용자 정보와 권한을 조회")
        void createAccessToken_success() {
            // given
            String username = "user1";
            Role role = Role.USER;

            // when
            String token =
                    jwtTokenProvider.createAccessToken(
                            username,
                            role
                    );

            // then
            assertThat(token)
                    .isNotBlank();

            assertThat(jwtTokenProvider.isValid(token))
                    .isTrue();

            assertThat(jwtTokenProvider.getUsername(token))
                    .isEqualTo(username);

            assertThat(jwtTokenProvider.getRole(token))
                    .isEqualTo(role);
        }

        @Test
        @DisplayName("생성된 JWT에 issuer와 jti를 포함")
        void createAccessToken_containsClaims() {
            // given
            String username = "user1";
            Role role = Role.USER;

            String token =
                    jwtTokenProvider.createAccessToken(
                            username,
                            role
                    );

            // when
            JWTClaimsSet claims =
                    jwtTokenProvider.parseClaims(token);

            // then
            assertThat(claims.getIssuer())
                    .isEqualTo(ISSUER);

            assertThat(claims.getJWTID())
                    .isNotBlank();

            assertThat(claims.getIssueTime())
                    .isNotNull();

            assertThat(claims.getExpirationTime())
                    .isNotNull();

            assertThat(
                    claims.getExpirationTime()
                            .after(claims.getIssueTime())
            ).isTrue();
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshToken {

        @Test
        @DisplayName("Refresh Token을 생성하고 검증")
        void createRefreshToken_success() {
            // given
            String username = "user1";
            Role role = Role.USER;

            // when
            String token =
                    jwtTokenProvider.createRefreshToken(
                            username,
                            role
                    );

            // then
            assertThat(token)
                    .isNotBlank();

            assertThat(jwtTokenProvider.isValid(token))
                    .isTrue();

            assertThat(jwtTokenProvider.getUsername(token))
                    .isEqualTo(username);

            assertThat(jwtTokenProvider.getRole(token))
                    .isEqualTo(role);
        }

        @Test
        @DisplayName("동일한 사용자로 연속 생성해도 서로 다른 Refresh Token을 생성")
        void createRefreshToken_unique() {
            // given
            String username = "user1";
            Role role = Role.USER;

            // when
            String firstToken =
                    jwtTokenProvider.createRefreshToken(
                            username,
                            role
                    );

            String secondToken =
                    jwtTokenProvider.createRefreshToken(
                            username,
                            role
                    );

            // then
            assertThat(secondToken)
                    .isNotEqualTo(firstToken);

            assertThat(
                    jwtTokenProvider
                            .parseClaims(firstToken)
                            .getJWTID()
            ).isNotEqualTo(
                    jwtTokenProvider
                            .parseClaims(secondToken)
                            .getJWTID()
            );
        }
    }

    @Nested
    @DisplayName("JWT 검증")
    class Validation {

        @Test
        @DisplayName("다른 Secret으로 서명된 JWT는 유효하지 않음")
        void isValid_fail_wrongSecret() {
            // given
            JwtTokenProvider otherProvider =
                    new JwtTokenProvider(
                            "abcdefghijklmnopqrstuvwxyz123456",
                            ACCESS_TOKEN_VALIDITY,
                            REFRESH_TOKEN_VALIDITY,
                            ISSUER
                    );

            String token =
                    otherProvider.createAccessToken(
                            "user1",
                            Role.USER
                    );

            // when
            boolean valid =
                    jwtTokenProvider.isValid(token);

            // then
            assertThat(valid)
                    .isFalse();
        }

        @Test
        @DisplayName("JWT 형식이 아닌 문자열은 유효하지 않음")
        void isValid_fail_invalidToken() {
            // given
            String token = "invalid-token";

            // when
            boolean valid =
                    jwtTokenProvider.isValid(token);

            // then
            assertThat(valid)
                    .isFalse();
        }
    }
}