package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.events.UserCacheClearEvent;
import com.sprint.mission.discodeit.events.UserEvent;
import com.sprint.mission.discodeit.events.UserEventType;
import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import com.sprint.mission.discodeit.service.basic.UserReader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 실제 로그아웃 핸들러의 토큰 검증·세션 무효화·사용자 이벤트 발행 계약을 검증한다.
 * Registry와 Reader 등은 Mock으로 두어 유효 세션이나 사용자 조회 결과에 따른 분기를 관찰한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtLogoutHandler 단위 테스트")
class JwtLogoutHandlerTest {
    @Mock ApplicationEventPublisher eventPublisher;


    @Mock
    RefreshTokenCookieManager cookieManager;

    @Mock
    JwtRegistry jwtRegistry;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    UserReader userReader;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("인증 객체 유무와 관계없이 쿠키의 사용자 ID와 리프레시 토큰으로 해당 토큰 쌍을 무효화한다")
    void logout_invalidatesMatchingRefreshTokenAndClearsCookie(boolean authenticated) {
        JwtLogoutHandler handler = new JwtLogoutHandler(
                cookieManager, jwtRegistry, jwtTokenProvider, userReader, eventPublisher);
        UUID userId = UUID.randomUUID();
        Claims claims = Jwts.claims().subject("cookie-user").build();
        given(cookieManager.readRefreshToken()).willReturn(Optional.of("refresh-token"));
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(JwtTokenProvider.TokenType.REFRESH.name());
        given(jwtTokenProvider.getUserId(claims)).willReturn(userId);
        given(userReader.findUserDto(userId)).willReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authenticated
                ? new UsernamePasswordAuthenticationToken("user", null, List.of())
                : null;

        handler.logout(request, response, authentication);

        // 실제 삭제 쿠키의 속성과 응답 헤더는 매니저 단위 테스트와 HTTP 통합 테스트에서 검증한다.
        verify(cookieManager).clearRefreshTokenCookie();
        verify(jwtRegistry).invalidateJwtInformationByRefreshToken(userId, "refresh-token");
        verify(userReader).findUserDto(userId);
        verify(eventPublisher).publishEvent(new UserCacheClearEvent(userId));
        verify(jwtRegistry, never()).invalidateJwtInformationByUserId(userId);
        assertThat(response.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
    }

    @ParameterizedTest(name = "online={0}")
    @ValueSource(booleans = {false, true})
    @DisplayName("조회된 사용자의 온라인 상태를 유지하여 수정 이벤트를 발행한다")
    void logout_publishesUpdatedEventWithReaderResult(boolean online) {
        // given
        JwtLogoutHandler handler = new JwtLogoutHandler(
                cookieManager, jwtRegistry, jwtTokenProvider, userReader, eventPublisher);
        UUID userId = UUID.randomUUID();
        UserDto user = new UserDto(
                userId, "user", "user@example.com", null, online, Role.USER, null, null);
        Claims claims = Jwts.claims().subject("user").build();

        given(cookieManager.readRefreshToken()).willReturn(Optional.of("refresh-token"));
        given(jwtTokenProvider.validateToken("refresh-token")).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(JwtTokenProvider.TokenType.REFRESH.name());
        given(jwtTokenProvider.getUserId(claims)).willReturn(userId);
        given(userReader.findUserDto(userId)).willReturn(Optional.of(user));

        // when
        handler.logout(new MockHttpServletRequest(), new MockHttpServletResponse(), null);

        // then
        ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(UserEventType.UPDATED);
        assertThat(event.user()).isSameAs(user);
        assertThat(event.user().online()).isEqualTo(online);
    }

    @Test
    @DisplayName("리프레시 쿠키가 없어도 쿠키 삭제 응답을 생성한다")
    void logout_clearsCookieWhenTokenIsMissing() {
        given(cookieManager.readRefreshToken()).willReturn(Optional.empty());

        new JwtLogoutHandler(cookieManager, jwtRegistry, jwtTokenProvider, userReader, eventPublisher).logout(
                new MockHttpServletRequest(), new MockHttpServletResponse(), null);

        verify(cookieManager).clearRefreshTokenCookie();
        verifyNoInteractions(jwtTokenProvider, jwtRegistry, userReader, eventPublisher);
    }

    @Test
    @DisplayName("검증에 실패한 쿠키는 다른 사용자 토큰을 무효화하지 않고 삭제한다")
    void logout_clearsInvalidCookieWithoutInvalidation() {
        given(cookieManager.readRefreshToken()).willReturn(Optional.of("invalid-token"));
        given(jwtTokenProvider.validateToken("invalid-token")).willReturn(Optional.empty());

        new JwtLogoutHandler(cookieManager, jwtRegistry, jwtTokenProvider, userReader, eventPublisher).logout(
                new MockHttpServletRequest(), new MockHttpServletResponse(), null);

        verify(cookieManager).clearRefreshTokenCookie();
        verifyNoInteractions(jwtRegistry, userReader, eventPublisher);
    }

    @Test
    @DisplayName("리프레시 쿠키에 ACCESS 토큰이 들어오면 토큰을 무효화하지 않고 쿠키만 삭제한다")
    void logout_rejectsAccessTokenInRefreshCookie() {
        Claims claims = Jwts.claims().subject("cookie-user").build();
        given(cookieManager.readRefreshToken()).willReturn(Optional.of("access-token"));
        given(jwtTokenProvider.validateToken("access-token")).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(JwtTokenProvider.TokenType.ACCESS.name());

        new JwtLogoutHandler(cookieManager, jwtRegistry, jwtTokenProvider, userReader, eventPublisher).logout(
                new MockHttpServletRequest(), new MockHttpServletResponse(), null);

        verify(cookieManager).clearRefreshTokenCookie();
        verifyNoInteractions(jwtRegistry, userReader, eventPublisher);
    }
}
