package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private static final String SECRET =
            "01234567890123456789012345678901";

    private static final long ACCESS_TOKEN_VALIDITY =
            60 * 60 * 1000L;

    private static final long REFRESH_TOKEN_VALIDITY =
            14 * 24 * 60 * 60 * 1000L;

    private final JwtTokenProvider jwtTokenProvider =
            new JwtTokenProvider(
                    SECRET,
                    ACCESS_TOKEN_VALIDITY,
                    REFRESH_TOKEN_VALIDITY
            );

    @Test
    @DisplayName("Access Token을 발급할 수 있다")
    void should_GenerateAccessToken_when_ValidUserInformationProvided() {
        // given
        UUID userId = UUID.randomUUID();
        String username = "tester";
        Role role = Role.USER;

        // when
        String token =
                jwtTokenProvider.generateAccessToken(
                        userId,
                        username,
                        role
                );

        // then
        assertThat(token).isNotBlank();
        assertThat(
                jwtTokenProvider.validateToken(token)
        ).isTrue();
    }

    @Test
    @DisplayName("Refresh Token을 발급할 수 있다")
    void should_GenerateRefreshToken_when_ValidUserInformationProvided() {
        // given
        UUID userId = UUID.randomUUID();
        String username = "tester";
        Role role = Role.USER;

        // when
        String token =
                jwtTokenProvider.generateRefreshToken(
                        userId,
                        username,
                        role
                );

        // then
        assertThat(token).isNotBlank();
        assertThat(
                jwtTokenProvider.validateToken(token)
        ).isTrue();
    }

    @Test
    @DisplayName("유효한 JWT는 true를 반환한다")
    void should_ReturnTrue_when_TokenIsValid() {
        // given
        String token =
                jwtTokenProvider.generateAccessToken(
                        UUID.randomUUID(),
                        "tester",
                        Role.USER
                );

        // when
        boolean result =
                jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT는 false를 반환한다")
    void should_ReturnFalse_when_TokenIsInvalid() {
        // given
        String token = "invalid.jwt.token";

        // when
        boolean result =
                jwtTokenProvider.validateToken(token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Refresh Token으로 새로운 Access Token을 발급할 수 있다")
    void should_RefreshAccessToken_when_RefreshTokenIsValid() {
        // given
        UUID userId = UUID.randomUUID();
        String username = "tester";
        Role role = Role.USER;

        String refreshToken =
                jwtTokenProvider.generateRefreshToken(
                        userId,
                        username,
                        role
                );

        // when
        String newAccessToken =
                jwtTokenProvider.refreshAccessToken(
                        refreshToken
                );

        // then
        assertThat(newAccessToken).isNotBlank();

        assertThat(
                jwtTokenProvider.validateToken(
                        newAccessToken
                )
        ).isTrue();

        assertThat(
                jwtTokenProvider.getUserId(
                        newAccessToken
                )
        ).isEqualTo(userId);

        assertThat(
                jwtTokenProvider.getUsername(
                        newAccessToken
                )
        ).isEqualTo(username);

        assertThat(
                jwtTokenProvider.getRole(
                        newAccessToken
                )
        ).isEqualTo(role);
    }

    @Test
    @DisplayName("Access Token으로 갱신을 시도하면 예외가 발생한다")
    void should_ThrowException_when_AccessTokenUsedAsRefreshToken() {
        // given
        String accessToken =
                jwtTokenProvider.generateAccessToken(
                        UUID.randomUUID(),
                        "tester",
                        Role.USER
                );

        // when & then
        assertThatThrownBy(() ->
                jwtTokenProvider.refreshAccessToken(
                        accessToken
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    @DisplayName("JWT에서 사용자 정보를 조회할 수 있다")
    void should_ReturnUserClaims_when_TokenIsValid() {
        // given
        UUID userId = UUID.randomUUID();
        String username = "tester";
        Role role = Role.CHANNEL_MANAGER;

        String token =
                jwtTokenProvider.generateAccessToken(
                        userId,
                        username,
                        role
                );

        // when & then
        assertThat(
                jwtTokenProvider.getUserId(token)
        ).isEqualTo(userId);

        assertThat(
                jwtTokenProvider.getUsername(token)
        ).isEqualTo(username);

        assertThat(
                jwtTokenProvider.getRole(token)
        ).isEqualTo(role);
    }
}