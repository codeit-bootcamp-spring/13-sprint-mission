package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryJwtRegistryTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private InMemoryJwtRegistry jwtRegistry;

    @BeforeEach
    void setUp() {

        /*
         * 최대 동시 로그인 수 = 1
         */
        jwtRegistry =
                new InMemoryJwtRegistry(
                        1,
                        jwtTokenProvider
                );
    }

    @Nested
    @DisplayName("JWT 정보 등록")
    class RegisterJwtInformationTest {

        @Test
        @DisplayName(
                "JWT 정보를 등록하면 userId, accessToken, refreshToken으로 조회할 수 있다"
        )
        void registerJwtInformation_validJwtInformation_registersSuccessfully() {

            // given
            UUID userId =
                    UUID.randomUUID();

            String accessToken =
                    "access-token-1";

            String refreshToken =
                    "refresh-token-1";

            JwtInformation jwtInformation =
                    createJwtInformation(
                            userId,
                            accessToken,
                            refreshToken
                    );

            // when
            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    userId
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    accessToken
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    refreshToken
                            )
            ).isTrue();
        }

        @Test
        @DisplayName(
                "최대 로그인 수가 1이면 같은 사용자의 새로운 JWT 등록 시 기존 JWT를 제거한다"
        )
        void registerJwtInformation_sameUserRegisteredAgain_removesPreviousJwtInformation() {

            // given
            UUID userId =
                    UUID.randomUUID();

            String oldAccessToken =
                    "old-access-token";

            String oldRefreshToken =
                    "old-refresh-token";

            JwtInformation oldJwtInformation =
                    createJwtInformation(
                            userId,
                            oldAccessToken,
                            oldRefreshToken
                    );

            jwtRegistry.registerJwtInformation(
                    oldJwtInformation
            );

            String newAccessToken =
                    "new-access-token";

            String newRefreshToken =
                    "new-refresh-token";

            JwtInformation newJwtInformation =
                    createJwtInformation(
                            userId,
                            newAccessToken,
                            newRefreshToken
                    );

            // when
            jwtRegistry.registerJwtInformation(
                    newJwtInformation
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    userId
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    oldAccessToken
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    oldRefreshToken
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    newAccessToken
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    newRefreshToken
                            )
            ).isTrue();
        }

        @Test
        @DisplayName(
                "서로 다른 사용자의 JWT 정보는 독립적으로 등록된다"
        )
        void registerJwtInformation_differentUsers_keepsBothJwtInformation() {

            // given
            UUID firstUserId =
                    UUID.randomUUID();

            UUID secondUserId =
                    UUID.randomUUID();

            JwtInformation firstJwtInformation =
                    createJwtInformation(
                            firstUserId,
                            "access-token-user-1",
                            "refresh-token-user-1"
                    );

            JwtInformation secondJwtInformation =
                    createJwtInformation(
                            secondUserId,
                            "access-token-user-2",
                            "refresh-token-user-2"
                    );

            // when
            jwtRegistry.registerJwtInformation(
                    firstJwtInformation
            );

            jwtRegistry.registerJwtInformation(
                    secondJwtInformation
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    firstUserId
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    secondUserId
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token-user-1"
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "access-token-user-2"
                            )
            ).isTrue();
        }
    }

    @Nested
    @DisplayName("JWT 정보 무효화")
    class InvalidateJwtInformationTest {

        @Test
        @DisplayName(
                "userId로 JWT 정보를 무효화하면 Access Token과 Refresh Token도 사용할 수 없다"
        )
        void invalidateJwtInformationByUserId_registeredUser_removesJwtInformation() {

            // given
            UUID userId =
                    UUID.randomUUID();

            String accessToken =
                    "access-token";

            String refreshToken =
                    "refresh-token";

            JwtInformation jwtInformation =
                    createJwtInformation(
                            userId,
                            accessToken,
                            refreshToken
                    );

            jwtRegistry.registerJwtInformation(
                    jwtInformation
            );

            // when
            jwtRegistry
                    .invalidateJwtInformationByUserId(
                            userId
                    );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    userId
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    accessToken
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    refreshToken
                            )
            ).isFalse();
        }

        @Test
        @DisplayName(
                "한 사용자를 무효화해도 다른 사용자의 JWT 정보는 유지된다"
        )
        void invalidateJwtInformationByUserId_multipleUsers_removesOnlyTargetUser() {

            // given
            UUID firstUserId =
                    UUID.randomUUID();

            UUID secondUserId =
                    UUID.randomUUID();

            jwtRegistry.registerJwtInformation(
                    createJwtInformation(
                            firstUserId,
                            "access-token-1",
                            "refresh-token-1"
                    )
            );

            jwtRegistry.registerJwtInformation(
                    createJwtInformation(
                            secondUserId,
                            "access-token-2",
                            "refresh-token-2"
                    )
            );

            // when
            jwtRegistry
                    .invalidateJwtInformationByUserId(
                            firstUserId
                    );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    firstUserId
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    secondUserId
                            )
            ).isTrue();

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
    @DisplayName("Refresh Token Rotation")
    class RotateJwtInformationTest {

        @Test
        @DisplayName(
                "Refresh Token을 Rotation하면 기존 JWT는 제거되고 새로운 JWT가 활성화된다"
        )
        void rotateJwtInformation_validRefreshToken_replacesJwtInformation() {

            // given
            UUID userId =
                    UUID.randomUUID();

            String oldAccessToken =
                    "old-access-token";

            String oldRefreshToken =
                    "old-refresh-token";

            JwtInformation oldJwtInformation =
                    createJwtInformation(
                            userId,
                            oldAccessToken,
                            oldRefreshToken
                    );

            jwtRegistry.registerJwtInformation(
                    oldJwtInformation
            );

            String newAccessToken =
                    "new-access-token";

            String newRefreshToken =
                    "new-refresh-token";

            JwtInformation newJwtInformation =
                    createJwtInformation(
                            userId,
                            newAccessToken,
                            newRefreshToken
                    );

            // when
            jwtRegistry.rotateJwtInformation(
                    oldRefreshToken,
                    newJwtInformation
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    oldAccessToken
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    oldRefreshToken
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    newAccessToken
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    newRefreshToken
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    userId
                            )
            ).isTrue();
        }

        @Test
        @DisplayName(
                "Registry에 없는 Refresh Token으로 Rotation하면 기존 JWT 정보는 유지된다"
        )
        void rotateJwtInformation_unknownRefreshToken_doesNotReplaceJwtInformation() {

            // given
            UUID userId =
                    UUID.randomUUID();

            JwtInformation oldJwtInformation =
                    createJwtInformation(
                            userId,
                            "old-access-token",
                            "old-refresh-token"
                    );

            jwtRegistry.registerJwtInformation(
                    oldJwtInformation
            );

            JwtInformation newJwtInformation =
                    createJwtInformation(
                            userId,
                            "new-access-token",
                            "new-refresh-token"
                    );

            // when
            jwtRegistry.rotateJwtInformation(
                    "unknown-refresh-token",
                    newJwtInformation
            );

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "old-access-token"
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "old-refresh-token"
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "new-access-token"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "new-refresh-token"
                            )
            ).isFalse();
        }
    }

    @Nested
    @DisplayName("만료된 JWT 정리")
    class ClearExpiredJwtInformationTest {

        @Test
        @DisplayName(
                "만료된 Refresh Token을 가진 JWT 정보는 Registry에서 제거한다"
        )
        void clearExpiredJwtInformation_expiredRefreshToken_removesJwtInformation() {

            // given
            UUID userId =
                    UUID.randomUUID();

            String accessToken =
                    "access-token";

            String refreshToken =
                    "expired-refresh-token";

            jwtRegistry.registerJwtInformation(
                    createJwtInformation(
                            userId,
                            accessToken,
                            refreshToken
                    )
            );

            when(
                    jwtTokenProvider
                            .validateRefreshToken(
                                    refreshToken
                            )
            ).thenReturn(false);

            // when
            jwtRegistry
                    .clearExpiredJwtInformation();

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    userId
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    accessToken
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    refreshToken
                            )
            ).isFalse();
        }

        @Test
        @DisplayName(
                "유효한 Refresh Token을 가진 JWT 정보는 Registry에 유지한다"
        )
        void clearExpiredJwtInformation_validRefreshToken_keepsJwtInformation() {

            // given
            UUID userId =
                    UUID.randomUUID();

            String accessToken =
                    "access-token";

            String refreshToken =
                    "valid-refresh-token";

            jwtRegistry.registerJwtInformation(
                    createJwtInformation(
                            userId,
                            accessToken,
                            refreshToken
                    )
            );

            when(
                    jwtTokenProvider
                            .validateRefreshToken(
                                    refreshToken
                            )
            ).thenReturn(true);

            // when
            jwtRegistry
                    .clearExpiredJwtInformation();

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    userId
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    accessToken
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    refreshToken
                            )
            ).isTrue();
        }

        @Test
        @DisplayName(
                "만료된 JWT만 제거하고 유효한 JWT는 유지한다"
        )
        void clearExpiredJwtInformation_mixedJwtInformation_removesOnlyExpiredJwt() {

            // given
            UUID expiredUserId =
                    UUID.randomUUID();

            UUID validUserId =
                    UUID.randomUUID();

            String expiredRefreshToken =
                    "expired-refresh-token";

            String validRefreshToken =
                    "valid-refresh-token";

            jwtRegistry.registerJwtInformation(
                    createJwtInformation(
                            expiredUserId,
                            "expired-access-token",
                            expiredRefreshToken
                    )
            );

            jwtRegistry.registerJwtInformation(
                    createJwtInformation(
                            validUserId,
                            "valid-access-token",
                            validRefreshToken
                    )
            );

            when(
                    jwtTokenProvider
                            .validateRefreshToken(
                                    expiredRefreshToken
                            )
            ).thenReturn(false);

            when(
                    jwtTokenProvider
                            .validateRefreshToken(
                                    validRefreshToken
                            )
            ).thenReturn(true);

            // when
            jwtRegistry
                    .clearExpiredJwtInformation();

            // then
            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    expiredUserId
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    validUserId
                            )
            ).isTrue();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "expired-access-token"
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "valid-access-token"
                            )
            ).isTrue();
        }
    }

    @Nested
    @DisplayName("잘못된 입력 처리")
    class InvalidInputTest {

        @Test
        @DisplayName(
                "null userId는 활성 JWT 정보가 없는 것으로 판단한다"
        )
        void hasActiveJwtInformationByUserId_null_returnsFalse() {

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByUserId(
                                    null
                            )
            ).isFalse();
        }

        @Test
        @DisplayName(
                "null 또는 빈 Access Token은 활성 JWT 정보가 없는 것으로 판단한다"
        )
        void hasActiveJwtInformationByAccessToken_invalidToken_returnsFalse() {

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    null
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    ""
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(
                                    "   "
                            )
            ).isFalse();
        }

        @Test
        @DisplayName(
                "null 또는 빈 Refresh Token은 활성 JWT 정보가 없는 것으로 판단한다"
        )
        void hasActiveJwtInformationByRefreshToken_invalidToken_returnsFalse() {

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    null
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    ""
                            )
            ).isFalse();

            assertThat(
                    jwtRegistry
                            .hasActiveJwtInformationByRefreshToken(
                                    "   "
                            )
            ).isFalse();
        }
    }

    /**
     * 테스트용 JwtInformation 생성.
     *
     * UserResponse 전체 객체를 직접 생성할 필요 없이
     * Mockito로 userId만 반환하도록 구성한다.
     */
    private JwtInformation createJwtInformation(
            UUID userId,
            String accessToken,
            String refreshToken
    ) {

        UserResponse userResponse =
                org.mockito.Mockito.mock(
                        UserResponse.class
                );

        when(
                userResponse.getId()
        ).thenReturn(
                userId
        );

        return new JwtInformation(
                userResponse,
                accessToken,
                refreshToken
        );
    }
}