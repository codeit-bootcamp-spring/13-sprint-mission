package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.events.UserCacheClearEvent;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.given;

/**
 * 실제 인메모리 레지스트리의 토큰 변경과 사용자별 동시성 처리를 검증한다.
 * 토큰 검증기와 추적 저장소는 Mock으로 제어하며 원자적 추적 제거의 호출 조건을 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("인메모리 JWT 레지스트리 단위 테스트")
class InMemoryJwtRegistryTest {
    private static final Instant NOW = Instant.parse("2026-10-11T00:00:00Z");

    @Mock ApplicationEventPublisher eventPublisher;
    @Mock UserOnlineTrackingRepository trackingRepository;


    @Mock
    JwtTokenProvider jwtTokenProvider;

    InMemoryJwtRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryJwtRegistry(jwtTokenProvider, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC), trackingRepository);
        given(jwtTokenProvider.getMaxActiveJwtCount()).willReturn(1);
    }

    @Test
    @DisplayName("첫 로그인 정보를 등록하면 사용자와 두 토큰으로 조회할 수 있다")
    void register_makesUserAndTokensActive() {
        JwtInformation info = information(UUID.randomUUID());
        stubValidRefresh(info);
        assertActive(info, false);

        registry.registerJwtInformation(info);

        assertActive(info, true);
    }

    @Test
    @DisplayName("같은 사용자의 재로그인은 기존 토큰을 대체하고 다른 사용자는 유지한다")
    void register_replacesOnlySameUserTokens() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation replacement = information(old.userDto().id());
        JwtInformation other = information(UUID.randomUUID());
        stubValidRefresh(replacement, other);
        registry.registerJwtInformation(old);
        registry.registerJwtInformation(other);

        registry.registerJwtInformation(replacement);

        assertTokensActive(old, false);
        assertActive(replacement, true);
        assertActive(other, true);
    }

    @Test
    @DisplayName("사용자 ID로 전체 토큰을 무효화하며 다른 사용자는 유지한다")
    void invalidate_removesOnlyRequestedUser() {
        JwtInformation target = information(UUID.randomUUID());
        JwtInformation other = information(UUID.randomUUID());
        stubValidRefresh(other);
        registry.registerJwtInformation(target);
        registry.registerJwtInformation(other);

        registry.invalidateJwtInformationByUserId(target.userDto().id());
        registry.invalidateJwtInformationByUserId(target.userDto().id());

        assertActive(target, false);
        assertActive(other, true);
    }

    @Test
    @DisplayName("일치하는 리프레시 토큰의 무효화는 사용자와 토큰 쌍을 제거하고 반복해도 다른 사용자를 유지한다")
    void invalidateByRefreshToken_removesMatchingPairAndAllowsRepeatedRequests() {
        JwtInformation target = information(UUID.randomUUID());
        JwtInformation other = information(UUID.randomUUID());
        stubValidRefresh(other);
        registry.registerJwtInformation(target);
        registry.registerJwtInformation(other);

        registry.invalidateJwtInformationByRefreshToken(target.userDto().id(), target.refreshToken());
        registry.invalidateJwtInformationByRefreshToken(target.userDto().id(), target.refreshToken());

        assertActive(target, false);
        assertActive(other, true);
    }

    @Test
    @DisplayName("다른 사용자의 리프레시 토큰으로 무효화를 요청해도 두 사용자 모두 유지한다")
    void invalidateByRefreshToken_preservesUsersWhenTokenDoesNotMatch() {
        JwtInformation target = information(UUID.randomUUID());
        JwtInformation other = information(UUID.randomUUID());
        stubValidRefresh(target, other);
        registry.registerJwtInformation(target);
        registry.registerJwtInformation(other);

        registry.invalidateJwtInformationByRefreshToken(target.userDto().id(), other.refreshToken());

        assertActive(target, true);
        assertActive(other, true);
    }

    @Test
    @DisplayName("이전 로그인 토큰의 무효화 요청은 새 로그인 정보를 삭제하지 않는다")
    void invalidateByRefreshToken_preservesNewLogin() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation current = information(old.userDto().id());
        stubValidRefresh(current);
        registry.registerJwtInformation(old);
        registry.registerJwtInformation(current);

        registry.invalidateJwtInformationByRefreshToken(old.userDto().id(), old.refreshToken());

        assertTokensActive(old, false);
        assertActive(current, true);
    }

    @Test
    @DisplayName("갱신 전 토큰의 무효화 요청은 갱신된 토큰 쌍을 삭제하지 않는다")
    void invalidateByRefreshToken_preservesRotatedTokens() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation current = information(old.userDto().id());
        stubValidRefresh(current);
        registry.registerJwtInformation(old);
        registry.rotateJwtInformation(old.refreshToken(), current);

        registry.invalidateJwtInformationByRefreshToken(old.userDto().id(), old.refreshToken());

        assertTokensActive(old, false);
        assertActive(current, true);
    }

    @Test
    @DisplayName("로그아웃이 먼저 완료되면 해당 토큰의 갱신은 실패하고 사용자를 다시 등록하지 않는다")
    void rotate_rejectsTokenInvalidatedByLogout() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation replacement = information(old.userDto().id());
        registry.registerJwtInformation(old);
        registry.invalidateJwtInformationByRefreshToken(old.userDto().id(), old.refreshToken());

        assertThatThrownBy(() -> registry.rotateJwtInformation(old.refreshToken(), replacement))
                .isInstanceOf(TokenRenewalFailedException.class);

        assertActive(old, false);
        assertActive(replacement, false);
    }

    @Test
    @DisplayName("로테이션은 이전 두 토큰을 제거하고 새 토큰만 활성화한다")
    void rotate_replacesMatchingTokenPair() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation replacement = information(old.userDto().id());
        JwtInformation other = information(UUID.randomUUID());
        stubValidRefresh(replacement, other);
        registry.registerJwtInformation(old);
        registry.registerJwtInformation(other);

        registry.rotateJwtInformation(old.refreshToken(), replacement);

        assertTokensActive(old, false);
        assertActive(replacement, true);
        assertActive(other, true);
    }

    @Test
    @DisplayName("등록되지 않은 사용자의 로테이션은 실패하고 새 정보를 등록하지 않는다")
    void rotate_rejectsMissingUser() {
        JwtInformation replacement = information(UUID.randomUUID());

        assertThatThrownBy(() -> registry.rotateJwtInformation("missing", replacement))
                .isInstanceOf(TokenRenewalFailedException.class);

        assertActive(replacement, false);
    }

    @Test
    @DisplayName("다른 리프레시 토큰으로 로테이션하면 기존 상태를 유지하고 실패한다")
    void rotate_rejectsMismatchedTokenWithoutMutation() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation other = information(UUID.randomUUID());
        JwtInformation replacement = information(old.userDto().id());
        stubValidRefresh(old, other);
        registry.registerJwtInformation(old);
        registry.registerJwtInformation(other);

        assertThatThrownBy(() -> registry.rotateJwtInformation(other.refreshToken(), replacement))
                .isInstanceOf(TokenRenewalFailedException.class);

        assertActive(old, true);
        assertActive(other, true);
        assertTokensActive(replacement, false);
    }

    @Test
    @DisplayName("이미 교체한 리프레시 토큰은 재사용할 수 없고 현재 토큰은 유지한다")
    void rotate_rejectsReusedTokenWithoutMutation() {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation current = information(old.userDto().id());
        JwtInformation rejected = information(old.userDto().id());
        stubValidRefresh(current);
        registry.registerJwtInformation(old);
        registry.rotateJwtInformation(old.refreshToken(), current);

        assertThatThrownBy(() -> registry.rotateJwtInformation(old.refreshToken(), rejected))
                .isInstanceOf(TokenRenewalFailedException.class);

        assertTokensActive(old, false);
        assertActive(current, true);
        assertTokensActive(rejected, false);
    }

    @Test
    @DisplayName("리프레시 만료 정보와 빈 사용자 항목만 삭제하고 유효한 정보는 유지한다")
    void clearExpired_removesExpiredUserAndPreservesValidRefresh() {
        JwtInformation expired = information(UUID.randomUUID());
        JwtInformation valid = information(UUID.randomUUID());
        registry.registerJwtInformation(expired);
        registry.registerJwtInformation(valid);
        given(jwtTokenProvider.validateToken(expired.refreshToken())).willReturn(Optional.empty());
        given(jwtTokenProvider.validateToken(valid.refreshToken()))
                .willReturn(Optional.of(Jwts.claims().subject(valid.userDto().username()).build()));

        // 저장 정보가 아직 정리되지 않았어도 만료된 사용자는 즉시 오프라인이다.
        assertThat(registry.hasActiveJwtInformationByUserId(expired.userDto().id())).isFalse();
        assertThat(registry.findActiveUserIds(List.of(expired.userDto().id(), valid.userDto().id(),
                valid.userDto().id(), UUID.randomUUID()))).containsExactly(valid.userDto().id());
        assertThat(registry.findActiveUserIds(List.of())).isEmpty();

        registry.clearExpiredJwtInformation();
        registry.clearExpiredJwtInformation();

        assertActive(expired, false);
        assertActive(valid, true);
        org.mockito.Mockito.verify(eventPublisher).publishEvent(new UserCacheClearEvent(expired.userDto().id()));
        org.mockito.Mockito.verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("만료 정리 도중 시작한 새 로그인이 만료 정보와 함께 삭제되지 않는다")
    void clearExpired_preservesConcurrentNewLogin() throws Exception {
        JwtInformation expired = information(UUID.randomUUID());
        JwtInformation replacement = information(expired.userDto().id());
        stubValidRefresh(replacement);
        registry.registerJwtInformation(expired);
        CountDownLatch validationStarted = new CountDownLatch(1);
        CountDownLatch finishValidation = new CountDownLatch(1);
        CountDownLatch registrationStarted = new CountDownLatch(1);
        AtomicReference<Thread> registrationThread = new AtomicReference<>();
        given(jwtTokenProvider.validateToken(expired.refreshToken())).willAnswer(invocation -> {
            validationStarted.countDown();
            assertThat(finishValidation.await(5, SECONDS)).isTrue();
            return Optional.empty();
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> cleanup = executor.submit(registry::clearExpiredJwtInformation);
            assertThat(validationStarted.await(5, SECONDS)).isTrue();
            Future<?> registration = executor.submit(() -> {
                registrationThread.set(Thread.currentThread());
                registrationStarted.countDown();
                registry.registerJwtInformation(replacement);
            });
            assertThat(registrationStarted.await(5, SECONDS)).isTrue();
            // 기존 구현은 등록이 먼저 완료되고, 수정 구현은 동일 사용자 잠금에서 대기한다.
            // 어느 경우든 등록 시도 전 정리가 끝나는 우연한 실행 순서를 배제한다.
            await().atMost(Duration.ofSeconds(3)).until(() -> registration.isDone()
                    || registrationThread.get().getState() == Thread.State.BLOCKED);
            finishValidation.countDown();
            cleanup.get(5, SECONDS);
            registration.get(5, SECONDS);

            assertTokensActive(expired, false);
            assertActive(replacement, true);
        } finally {
            finishValidation.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("같은 리프레시 토큰의 동시 로테이션은 한 번만 성공한다")
    void rotate_allowsExactlyOneConcurrentReplacement() throws Exception {
        JwtInformation old = information(UUID.randomUUID());
        JwtInformation first = information(old.userDto().id());
        JwtInformation second = information(old.userDto().id());
        registry.registerJwtInformation(old);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> firstResult = executor.submit(() -> rotateAfterSignal(old, first, ready, start));
            Future<Boolean> secondResult = executor.submit(() -> rotateAfterSignal(old, second, ready, start));
            assertThat(ready.await(5, SECONDS)).isTrue();
            start.countDown();
            boolean firstSucceeded = firstResult.get(5, SECONDS);
            boolean secondSucceeded = secondResult.get(5, SECONDS);

            assertThat(firstSucceeded).isNotEqualTo(secondSucceeded);
            assertTokensActive(old, false);
            assertTokensActive(first, firstSucceeded);
            assertTokensActive(second, secondSucceeded);
            stubValidRefresh(firstSucceeded ? first : second);
            assertThat(registry.hasActiveJwtInformationByUserId(old.userDto().id())).isTrue();
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("최대 두 세션을 유지하고 갱신한 세션은 가장 최근 순서로 이동한다")
    void registerAndRotate_respectConfiguredLimitAndRecency() {
        given(jwtTokenProvider.getMaxActiveJwtCount()).willReturn(2);
        UUID userId = UUID.randomUUID();
        JwtInformation first = information(userId);
        JwtInformation second = information(userId);
        JwtInformation third = information(userId);
        JwtInformation rotatedSecond = information(userId);
        JwtInformation fourth = information(userId);
        registry.registerJwtInformation(first);
        registry.registerJwtInformation(second);
        assertTokensActive(first, true);
        assertTokensActive(second, true);

        registry.registerJwtInformation(third);
        assertTokensActive(first, false);
        assertTokensActive(second, true);
        assertTokensActive(third, true);

        registry.rotateJwtInformation(second.refreshToken(), rotatedSecond);
        registry.registerJwtInformation(fourth);
        assertTokensActive(second, false);
        assertTokensActive(third, false);
        assertTokensActive(rotatedSecond, true);
        assertTokensActive(fourth, true);
    }

    @Test
    @DisplayName("유효한 Access 세션이 하나라도 있으면 온라인이며 마지막 유효 세션 로그아웃 후에는 제외한다")
    void findUserIdsWithValidAccessToken_handlesMultipleSessionsAndLogout() {
        given(jwtTokenProvider.getMaxActiveJwtCount()).willReturn(2);
        UUID userId = UUID.randomUUID();
        JwtInformation expired = information(userId);
        JwtInformation valid = information(userId);
        stubValidRefresh(expired);
        given(jwtTokenProvider.validateToken(expired.accessToken())).willReturn(Optional.empty());
        stubAccessExpiration(valid, NOW.plusSeconds(60));
        registry.registerJwtInformation(expired);
        registry.registerJwtInformation(valid);

        assertThat(registry.findUserIdsWithValidAccessToken(List.of(userId, UUID.randomUUID(), userId)))
                .containsExactly(userId);
        assertThat(registry.findUserIdsWithValidAccessToken(List.of())).isEmpty();

        registry.invalidateJwtInformationByRefreshToken(userId, valid.refreshToken());
        assertThat(registry.findUserIdsWithValidAccessToken(List.of(userId))).isEmpty();
        assertThat(registry.hasActiveJwtInformationByUserId(userId)).isTrue();

        JwtInformation refreshed = information(userId);
        stubAccessExpiration(refreshed, NOW.plusSeconds(600));
        registry.rotateJwtInformation(expired.refreshToken(), refreshed);
        assertThat(registry.findUserIdsWithValidAccessToken(List.of(userId))).containsExactly(userId);

        registry.invalidateJwtInformationByUserId(userId);
        assertThat(registry.findUserIdsWithValidAccessToken(List.of(userId))).isEmpty();
    }

    @Test
    @DisplayName("만료된 세션과 만료 시각이 없는 Access Token은 온라인 조회에서 제외한다")
    void findUserIdsWithValidAccessToken_excludesExpiredSessionsAndMissingExpiration() {
        JwtInformation expiredSession = information(UUID.randomUUID());
        JwtInformation missingExpiration = information(UUID.randomUUID());
        given(jwtTokenProvider.validateToken(expiredSession.refreshToken())).willReturn(Optional.empty());
        stubAccessExpiration(missingExpiration, null);
        registry.registerJwtInformation(expiredSession);
        registry.registerJwtInformation(missingExpiration);

        assertThat(registry.findUserIdsWithValidAccessToken(List.of(expiredSession.userDto().id(),
                missingExpiration.userDto().id()))).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"1, true", "0, false", "-1, false"})
    @DisplayName("Access 만료 시각 직전까지만 온라인이며 만료 시각부터는 오프라인이다")
    void findUserIdsWithValidAccessToken_checksExpirationBoundary(long remainingSeconds, boolean online) {
        JwtInformation info = information(UUID.randomUUID());
        stubAccessExpiration(info, NOW.plusSeconds(remainingSeconds));
        registry.registerJwtInformation(info);

        assertThat(registry.findUserIdsWithValidAccessToken(List.of(info.userDto().id())).contains(info.userDto().id()))
                .isEqualTo(online);
    }

    private void stubAccessExpiration(JwtInformation info, Instant expiration) {
        stubValidRefresh(info);
        Claims claims = Jwts.claims()
                .id(info.accessToken())
                .add("user_id", info.userDto().id().toString())
                .add("token_type", JwtTokenProvider.TokenType.ACCESS.name())
                .expiration(expiration == null ? null : Date.from(expiration))
                .build();
        given(jwtTokenProvider.validateToken(info.accessToken())).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(JwtTokenProvider.TokenType.ACCESS.name());
        given(jwtTokenProvider.getUserId(claims)).willReturn(info.userDto().id());
    }

    private void stubValidRefresh(JwtInformation... infos) {
        for (JwtInformation info : infos) {
            given(jwtTokenProvider.validateToken(info.refreshToken()))
                    .willReturn(Optional.of(Jwts.claims().subject(info.userDto().username()).build()));
        }
    }

    private boolean rotateAfterSignal(JwtInformation old, JwtInformation replacement,
                                      CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        assertThat(start.await(5, SECONDS)).isTrue();
        try {
            registry.rotateJwtInformation(old.refreshToken(), replacement);
            return true;
        } catch (TokenRenewalFailedException e) {
            return false;
        }
    }

    private JwtInformation information(UUID userId) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-23T00:00:00Z");
        UserDto user = new UserDto(userId, "user-" + userId, "user@example.com", null, true,
                Role.USER, now, now);
        return new JwtInformation(user, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    private void assertActive(JwtInformation info, boolean active) {
        assertThat(registry.hasActiveJwtInformationByUserId(info.userDto().id())).isEqualTo(active);
        assertTokensActive(info, active);
    }

    private void assertTokensActive(JwtInformation info, boolean active) {
        assertThat(registry.hasActiveJwtInformationByAccessToken(info.accessToken())).isEqualTo(active);
        assertThat(registry.hasActiveJwtInformationByRefreshToken(info.refreshToken())).isEqualTo(active);
    }
}
