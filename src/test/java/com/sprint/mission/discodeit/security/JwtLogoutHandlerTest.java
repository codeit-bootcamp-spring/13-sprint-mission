package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtLogoutHandler 단위 테스트")
class JwtLogoutHandlerTest {

    @Mock
    RefreshTokenCookieManager cookieManager;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("인증 객체 유무와 관계없이 쿠키 삭제를 한 번 위임한다")
    void logout_delegatesCookieClearing(boolean authenticated) {
        JwtLogoutHandler handler = new JwtLogoutHandler(cookieManager);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = authenticated
                ? new UsernamePasswordAuthenticationToken("user", null, List.of())
                : null;

        handler.logout(request, response, authentication);

        // 실제 삭제 쿠키의 속성과 응답 헤더는 매니저 단위 테스트와 HTTP 통합 테스트에서 검증한다.
        verify(cookieManager).clearRefreshTokenCookie();
        verify(cookieManager, never()).readRefreshToken();
        assertThat(response.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
    }
}
