package com.sprint.mission.discodeit.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.SseEventListener;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import java.util.List;
import java.util.UUID;

import com.sprint.mission.discodeit.redis.RedisLockProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class RedisJwtRegistryTest {

    private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
    private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access_tokens";
    private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh_tokens";

    @Autowired
    private RedisJwtRegistry redisJwtRegistry;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApplicationEvents applicationEvents;

    @MockitoBean
    private SseEventListener sseEventListener;

    @MockitoSpyBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RedisLockProvider redisLockProvider;

    private UUID userId;
    private UserDto userDto;

    private String accessToken;
    private String refreshToken;

    private String newAccessToken;
    private String newRefreshToken;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userDto = new UserDto(
                userId,
                "testUser",
                "test@test.com",
                null,
                false,
                Role.USER
        );

        accessToken = "access-" + UUID.randomUUID();
        refreshToken = "refresh-" + UUID.randomUUID();

        newAccessToken = "new-access-" + UUID.randomUUID();
        newRefreshToken = "new-refresh-" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete(getUserKey());

        redisTemplate.opsForSet().remove(
                ACCESS_TOKEN_INDEX_KEY,
                accessToken,
                newAccessToken
        );

        redisTemplate.opsForSet().remove(
                REFRESH_TOKEN_INDEX_KEY,
                refreshToken,
                newRefreshToken
        );
    }

    @Test
    @DisplayName("JWT 등록 시 사용자 정보와 토큰 인덱스를 Redis에 저장")
    void register_success() {
        // given
        JwtInformation jwtInformation = createJwtInformation(
                accessToken,
                refreshToken
        );

        // when
        redisJwtRegistry.registerJwtInformation(jwtInformation);

        // then
        assertThat(redisJwtRegistry.hasActiveJwtInformationByUserId(userId))
                .isTrue();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .isTrue();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isTrue();

        List<Object> savedTokens = redisTemplate.opsForList()
                .range(getUserKey(), 0, -1);

        assertThat(savedTokens)
                .hasSize(1);

        JwtInformation saved = (JwtInformation) savedTokens.get(0);

        assertThat(saved.getUserDto().id())
                .isEqualTo(userId);

        assertThat(saved.getAccessToken())
                .isEqualTo(accessToken);

        assertThat(saved.getRefreshToken())
                .isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("JWT 등록 시 Refresh Token 유효기간을 기준으로 TTL 설정")
    void register_sets_ttl() {
        // given
        JwtInformation jwtInformation = createJwtInformation(
                accessToken,
                refreshToken
        );

        // when
        redisJwtRegistry.registerJwtInformation(jwtInformation);

        // then
        Long ttl = redisTemplate.getExpire(getUserKey());

        assertThat(ttl)
                .isNotNull()
                .isPositive();
    }

    @Test
    @DisplayName("사용자 ID로 JWT 무효화 시 모든 토큰 정보 삭제")
    void invalidate_by_user_id() {
        // given
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(accessToken, refreshToken)
        );

        // when
        redisJwtRegistry.invalidateJwtInformationByUserId(userId);

        // then
        assertThat(redisJwtRegistry.hasActiveJwtInformationByUserId(userId))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isFalse();

        assertThat(redisTemplate.hasKey(getUserKey()))
                .isFalse();
    }

    @Test
    @DisplayName("Refresh Token으로 무효화 시 해당 JWT 정보 삭제")
    void invalidate_by_refresh_token() {
        // given
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(accessToken, refreshToken)
        );

        // when
        redisJwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);

        // then
        assertThat(redisJwtRegistry.hasActiveJwtInformationByUserId(userId))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isFalse();
    }

    @Test
    @DisplayName("JWT 회전 시 기존 토큰은 무효화하고 새 토큰을 등록")
    void rotate_success() {
        // given
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(accessToken, refreshToken)
        );

        JwtInformation newJwtInformation = createJwtInformation(
                newAccessToken,
                newRefreshToken
        );

        // when
        redisJwtRegistry.rotateJwtInformation(
                refreshToken,
                newJwtInformation
        );

        // then
        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(newAccessToken))
                .isTrue();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(newRefreshToken))
                .isTrue();

        List<Object> savedTokens = redisTemplate.opsForList()
                .range(getUserKey(), 0, -1);

        assertThat(savedTokens)
                .hasSize(1);

        JwtInformation saved = (JwtInformation) savedTokens.get(0);

        assertThat(saved.getAccessToken())
                .isEqualTo(newAccessToken);

        assertThat(saved.getRefreshToken())
                .isEqualTo(newRefreshToken);
    }

    @Test
    @DisplayName("동시 로그인 제한 초과 시 기존 JWT를 제거하고 새 JWT 등록")
    void register_exceeds_max_active_count() {
        // given
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(accessToken, refreshToken)
        );

        // when
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(newAccessToken, newRefreshToken)
        );

        // then
        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(newAccessToken))
                .isTrue();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(newRefreshToken))
                .isTrue();

        assertThat(redisTemplate.opsForList().size(getUserKey()))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("JWT 정보가 없는 사용자는 오프라인 상태")
    void has_active_jwt_returns_false() {
        // when
        boolean online = redisJwtRegistry.hasActiveJwtInformationByUserId(userId);

        // then
        assertThat(online)
                .isFalse();
    }

    @Test
    @DisplayName("만료된 JWT 정리 시 사용자 정보와 토큰 인덱스를 삭제")
    void clear_expired_jwt_information() {
        // given
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(accessToken, refreshToken)
        );

        doReturn(false)
                .when(jwtTokenProvider)
                .isValid(refreshToken);

        // when
        redisJwtRegistry.clearExpiredJwtInformation();

        // then
        assertThat(redisJwtRegistry.hasActiveJwtInformationByUserId(userId))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .isFalse();

        assertThat(redisJwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isFalse();

        assertThat(redisTemplate.hasKey(getUserKey()))
                .isFalse();
    }

    @Test
    @DisplayName("최초 JWT 등록 시 로그인 이벤트를 발행하고 무효화 시 로그아웃 이벤트를 발행")
    void publish_login_logout_events() {
        // given
        JwtInformation jwtInformation = createJwtInformation(
                accessToken,
                refreshToken
        );

        // when
        redisJwtRegistry.registerJwtInformation(jwtInformation);

        // then
        assertThat(applicationEvents.stream(UserLogInOutEvent.class))
                .hasSize(1);

        // when
        redisJwtRegistry.invalidateJwtInformationByUserId(userId);

        // then
        assertThat(applicationEvents.stream(UserLogInOutEvent.class))
                .hasSize(2);
    }

    @Test
    @DisplayName("이미 로그인한 사용자의 JWT 재등록 시 로그인 이벤트를 중복 발행하지 않음")
    void register_does_not_publish_duplicate_login_event() {
        // given
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(accessToken, refreshToken)
        );

        // when
        redisJwtRegistry.registerJwtInformation(
                createJwtInformation(newAccessToken, newRefreshToken)
        );

        // then
        assertThat(applicationEvents.stream(UserLogInOutEvent.class))
                .hasSize(1);

        assertThat(redisJwtRegistry.hasActiveJwtInformationByUserId(userId))
                .isTrue();
    }

    @Test
    @DisplayName("서로 다른 RedisJwtRegistry 인스턴스에서 JWT 등록 및 무효화 상태를 공유")
    void jwt_information_shared_between_registry_instances() {
        // given
        RedisJwtRegistry registryA = redisJwtRegistry;

        RedisJwtRegistry registryB = new RedisJwtRegistry(
                jwtTokenProvider,
                eventPublisher,
                redisTemplate,
                redisLockProvider
        );

        JwtInformation jwtInformation = createJwtInformation(
                accessToken,
                refreshToken
        );

        // when: Registry A에서 JWT 등록
        registryA.registerJwtInformation(jwtInformation);

        // then: Registry B에서도 등록된 JWT 조회 가능
        assertThat(registryB.hasActiveJwtInformationByUserId(userId))
                .isTrue();

        assertThat(registryB.hasActiveJwtInformationByAccessToken(accessToken))
                .isTrue();

        assertThat(registryB.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isTrue();

        // when: Registry B에서 JWT 무효화
        registryB.invalidateJwtInformationByUserId(userId);

        // then: Registry A에서도 무효화 상태 확인
        assertThat(registryA.hasActiveJwtInformationByUserId(userId))
                .isFalse();

        assertThat(registryA.hasActiveJwtInformationByAccessToken(accessToken))
                .isFalse();

        assertThat(registryA.hasActiveJwtInformationByRefreshToken(refreshToken))
                .isFalse();
    }

    private JwtInformation createJwtInformation(
            String accessToken,
            String refreshToken
    ) {
        return new JwtInformation(
                userDto,
                accessToken,
                refreshToken
        );
    }

    private String getUserKey() {
        return USER_JWT_KEY_PREFIX + userId;
    }
}