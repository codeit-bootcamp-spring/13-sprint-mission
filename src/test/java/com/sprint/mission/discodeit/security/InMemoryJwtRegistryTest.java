package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InMemoryJwtRegistryTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private InMemoryJwtRegistry jwtRegistry;

    private UUID userId;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        jwtRegistry =
                new InMemoryJwtRegistry(jwtTokenProvider);

        userId = UUID.randomUUID();

        userDto = new UserDto(
                userId,
                "user1",
                "user1@test.com",
                null,
                true,
                Role.USER
        );
    }

    @Nested
    @DisplayName("JWT 등록")
    class Register {

        @Test
        @DisplayName("JWT 정보를 등록하면 활성 상태로 조회")
        void register_success() {
            // given
            JwtInformation jwtInformation =
                    new JwtInformation(
                            userDto,
                            "access-token",
                            "refresh-token"
                    );

            // when
            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(userId)
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token"
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token"
                            )
            ).isTrue();
        }

        @Test
        @DisplayName("동일 사용자가 다시 로그인하면 기존 JWT 정보를 제거")
        void register_exceedsMaxActiveJwtCount() {
            // given
            JwtInformation firstJwt =
                    new JwtInformation(
                            userDto,
                            "access-token-1",
                            "refresh-token-1"
                    );

            JwtInformation secondJwt =
                    new JwtInformation(
                            userDto,
                            "access-token-2",
                            "refresh-token-2"
                    );

            // when
            jwtRegistry.registerJwtInformation(firstJwt);
            jwtRegistry.registerJwtInformation(secondJwt);

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token-1"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token-1"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token-2"
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token-2"
                            )
            ).isTrue();
        }
    }

    @Nested
    @DisplayName("JWT Rotation")
    class Rotation {

        @Test
        @DisplayName("Refresh Token Rotation 시 기존 JWT를 제거하고 새 JWT를 등록")
        void rotate_success() {
            // given
            JwtInformation oldJwt =
                    new JwtInformation(
                            userDto,
                            "old-access-token",
                            "old-refresh-token"
                    );

            JwtInformation newJwt =
                    new JwtInformation(
                            userDto,
                            "new-access-token",
                            "new-refresh-token"
                    );

            jwtRegistry.registerJwtInformation(oldJwt);

            // when
            jwtRegistry.rotateJwtInformation(
                    "old-refresh-token",
                    newJwt
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "old-access-token"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "old-refresh-token"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "new-access-token"
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "new-refresh-token"
                            )
            ).isTrue();
        }
    }

    @Nested
    @DisplayName("JWT 무효화")
    class Invalidate {

        @Test
        @DisplayName("사용자 ID로 해당 사용자의 JWT 정보를 모두 제거")
        void invalidateByUserId_success() {
            // given
            JwtInformation jwtInformation =
                    new JwtInformation(
                            userDto,
                            "access-token",
                            "refresh-token"
                    );

            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            // when
            jwtRegistry.invalidateJwtInformationByUserId(
                    userId
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(userId)
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token"
                            )
            ).isFalse();
        }

        @Test
        @DisplayName("Refresh Token으로 해당 JWT 정보를 제거")
        void invalidateByRefreshToken_success() {
            // given
            JwtInformation jwtInformation =
                    new JwtInformation(
                            userDto,
                            "access-token",
                            "refresh-token"
                    );

            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            // when
            jwtRegistry
                    .invalidateJwtInformationByRefreshToken(
                            "refresh-token"
                    );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(userId)
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token"
                            )
            ).isFalse();
        }
    }

    @Nested
    @DisplayName("만료 JWT 정리")
    class ClearExpired {

        @Test
        @DisplayName("유효하지 않은 Refresh Token의 JWT 정보를 제거")
        void clearExpired_success() {
            // given
            JwtInformation jwtInformation =
                    new JwtInformation(
                            userDto,
                            "access-token",
                            "refresh-token"
                    );

            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            given(
                    jwtTokenProvider.isValid(
                            "refresh-token"
                    )
            ).willReturn(false);

            // when
            jwtRegistry.clearExpiredJwtInformation();

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(userId)
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token"
                            )
            ).isFalse();
        }

        @Test
        @DisplayName("유효한 Refresh Token의 JWT 정보는 유지")
        void clearExpired_keepsValidJwt() {
            // given
            JwtInformation jwtInformation =
                    new JwtInformation(
                            userDto,
                            "access-token",
                            "refresh-token"
                    );

            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            given(
                    jwtTokenProvider.isValid(
                            "refresh-token"
                    )
            ).willReturn(true);

            // when
            jwtRegistry.clearExpiredJwtInformation();

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(userId)
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "refresh-token"
                            )
            ).isTrue();
        }
    }
}