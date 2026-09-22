package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("실제 토큰 서비스·Provider·Registry 동시 갱신 컴포넌트 테스트")
class TokenRefreshConcurrencyTest {

    @Mock
    DiscodeitUserDetailsService userDetailsService;

    @Mock
    RefreshTokenCookieManager cookieManager;

    @Test
    @DisplayName("두 요청이 사전 활성 검사를 통과해도 한 요청만 등록된 새 토큰을 반환한다")
    void rotateRefreshToken_returnsOnlyOneSuccessfulPair() throws Exception {
        Instant now = Instant.parse("2026-09-23T00:00:00Z");
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties(
                "concurrency-test-secret-with-at-least-32-bytes", Duration.ofMinutes(10),
                Duration.ofDays(14), "concurrency-test"), Clock.fixed(now, ZoneOffset.UTC));
        InMemoryJwtRegistry registry = new InMemoryJwtRegistry(provider);
        TokenServiceImpl service = new TokenServiceImpl(provider, userDetailsService, cookieManager, registry);
        OffsetDateTime dateTime = now.atOffset(ZoneOffset.UTC);
        UserDto user = new UserDto(UUID.randomUUID(), "concurrent-user", "user@example.com",
                null, true, Role.USER, dateTime, dateTime);
        TokenDto initial = service.generateToken(user);
        registry.registerJwtInformation(new JwtInformation(user, initial.accessToken(), initial.refreshToken()));
        given(cookieManager.readRefreshToken()).willReturn(Optional.of(initial.refreshToken()));
        CyclicBarrier bothPassedActiveCheck = new CyclicBarrier(2);
        given(userDetailsService.loadUserByUsername(user.username())).willAnswer(invocation -> {
            // 사용자 조회 전에 두 요청 모두 동일한 기존 토큰의 활성 검사를 통과하도록 강제한다.
            bothPassedActiveCheck.await(5, SECONDS);
            return new DiscodeitUserDetails(user, "unused-password");
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Optional<JwtDtoWithRefresh>> first = executor.submit(() -> rotate(service));
            Future<Optional<JwtDtoWithRefresh>> second = executor.submit(() -> rotate(service));
            List<Optional<JwtDtoWithRefresh>> outcomes = List.of(first.get(10, SECONDS), second.get(10, SECONDS));
            List<JwtDtoWithRefresh> successful = outcomes.stream().flatMap(Optional::stream).toList();

            assertThat(successful).hasSize(1);
            JwtDtoWithRefresh winner = successful.get(0);
            assertThat(registry.hasActiveJwtInformationByAccessToken(winner.jwtDto().accessToken())).isTrue();
            assertThat(registry.hasActiveJwtInformationByRefreshToken(winner.refreshToken())).isTrue();
            assertThat(registry.hasActiveJwtInformationByAccessToken(initial.accessToken())).isFalse();
            assertThat(registry.hasActiveJwtInformationByRefreshToken(initial.refreshToken())).isFalse();
            // HTTP 계층이 성공 결과를 받은 뒤에만 쿠키를 쓴다.
            verify(cookieManager, never()).writeRefreshTokenCookie(any(), any());
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }

    private Optional<JwtDtoWithRefresh> rotate(TokenServiceImpl service) {
        try {
            return Optional.of(service.rotateRefreshToken());
        } catch (TokenRenewalFailedException e) {
            return Optional.empty();
        }
    }
}
