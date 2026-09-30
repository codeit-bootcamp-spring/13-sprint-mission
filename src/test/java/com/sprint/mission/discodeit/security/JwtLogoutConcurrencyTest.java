package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("실제 로그아웃 핸들러와 JWT 레지스트리의 동시성 테스트")
class JwtLogoutConcurrencyTest {

    @Mock
    RefreshTokenCookieManager cookieManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    enum TokenReplacement {
        LOGIN, REFRESH
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(TokenReplacement.class)
    @DisplayName("이전 로그아웃의 검증 도중 완료된 새 로그인이나 갱신의 토큰 쌍을 유지한다")
    void logout_preservesTokensReplacedDuringValidation(TokenReplacement replacement) throws Exception {
        // 핸들러와 레지스트리 사이의 동시 실행이 관심사이므로 두 객체 모두 실제 구현을 사용한다.
        InMemoryJwtRegistry registry = new InMemoryJwtRegistry(jwtTokenProvider);
        JwtLogoutHandler handler = new JwtLogoutHandler(cookieManager, registry, jwtTokenProvider);
        OffsetDateTime now = OffsetDateTime.parse("2026-09-27T00:00:00Z");
        UserDto user = new UserDto(UUID.randomUUID(), "logout-user", "logout@example.com", null,
                true, Role.USER, now, now);
        JwtInformation old = new JwtInformation(user, "old-access", "old-refresh");
        JwtInformation current = new JwtInformation(user, "new-access", "new-refresh");
        registry.registerJwtInformation(old);

        Claims claims = Jwts.claims().subject(user.username()).build();
        CountDownLatch validationStarted = new CountDownLatch(1);
        CountDownLatch finishValidation = new CountDownLatch(1);
        given(cookieManager.readRefreshToken()).willReturn(Optional.of(old.refreshToken()));
        given(jwtTokenProvider.validateToken(old.refreshToken())).willAnswer(invocation -> {
            validationStarted.countDown();
            assertThat(finishValidation.await(5, SECONDS)).isTrue();
            return Optional.of(claims);
        });
        given(jwtTokenProvider.getTokenType(claims)).willReturn(JwtTokenProvider.TokenType.REFRESH.name());
        given(jwtTokenProvider.getUserId(claims)).willReturn(user.id());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> logout = executor.submit(() -> handler.logout(
                    new MockHttpServletRequest(), new MockHttpServletResponse(), null));
            assertThat(validationStarted.await(5, SECONDS)).isTrue();
            switch (replacement) {
                case LOGIN -> registry.registerJwtInformation(current);
                case REFRESH -> registry.rotateJwtInformation(old.refreshToken(), current);
            }
            finishValidation.countDown();
            logout.get(5, SECONDS);

            assertThat(registry.hasActiveJwtInformationByAccessToken(old.accessToken())).isFalse();
            assertThat(registry.hasActiveJwtInformationByRefreshToken(old.refreshToken())).isFalse();
            assertThat(registry.hasActiveJwtInformationByAccessToken(current.accessToken())).isTrue();
            assertThat(registry.hasActiveJwtInformationByRefreshToken(current.refreshToken())).isTrue();
            assertThat(registry.hasActiveJwtInformationByUserId(user.id())).isTrue();
            verify(cookieManager).clearRefreshTokenCookie();
        } finally {
            finishValidation.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }
}
