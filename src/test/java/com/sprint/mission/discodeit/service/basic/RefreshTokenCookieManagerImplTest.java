package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.HttpCookie;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenCookieManagerImpl 단위 테스트")
class RefreshTokenCookieManagerImplTest {

    private static final String COOKIE_NAME = "REFRESH_TOKEN";
    private static final String TOKEN = "refresh-token";
    private static final String OTHER_COOKIE = "OTHER=other-value; Path=/";
    private static final String CSRF_COOKIE = "XSRF-TOKEN=csrf-value; Path=/";

    @Mock
    ObjectProvider<HttpServletResponse> responseProvider;

    @Mock
    ObjectProvider<HttpServletRequest> requestProvider;

    MockHttpServletRequest request;
    MockHttpServletResponse response;
    RefreshCookieProperties properties;
    RefreshTokenCookieManagerImpl manager;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("POST", "/api/auth/refresh");
        response = new MockHttpServletResponse();
        properties = new RefreshCookieProperties(true, false, "Lax");
        manager = new RefreshTokenCookieManagerImpl(responseProvider, requestProvider, properties);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "empty", "unrelated"})
    @DisplayName("리프레시 쿠키가 없으면 빈 값을 반환하고 응답 객체를 조회하지 않는다")
    void readRefreshToken_returnsEmptyWhenCookieMissing(String cookieState) {
        if (cookieState.equals("empty")) {
            request.setCookies(new Cookie[0]);
        } else if (cookieState.equals("unrelated")) {
            request.setCookies(new Cookie("OTHER", "other-value"));
        }
        given(requestProvider.getObject()).willReturn(request);

        assertThat(manager.readRefreshToken()).isEmpty();

        verifyNoInteractions(responseProvider);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("리프레시 쿠키 값이 null이거나 공백이면 빈 값을 반환한다")
    void readRefreshToken_returnsEmptyWhenValueBlank(String value) {
        request.setCookies(new Cookie(COOKIE_NAME, value));
        given(requestProvider.getObject()).willReturn(request);

        assertThat(manager.readRefreshToken()).isEmpty();

        verifyNoInteractions(responseProvider);
    }

    @Test
    @DisplayName("다른 쿠키 중 리프레시 쿠키를 읽고 요청 쿠키의 값과 속성을 변경하지 않는다")
    void readRefreshToken_readsMatchingCookieWithoutMutation() {
        Cookie refreshCookie = createRequestCookie();
        Cookie otherCookie = new Cookie("OTHER", "other-value");
        request.setCookies(otherCookie, refreshCookie);
        given(requestProvider.getObject()).willReturn(request);

        assertThat(manager.readRefreshToken()).contains(TOKEN);

        assertThat(request.getCookies()).containsExactly(otherCookie, refreshCookie);
        assertRequestCookieUnchanged(refreshCookie);
        verifyNoInteractions(responseProvider);
    }

    @ParameterizedTest
    @CsvSource({"true,false,Lax", "true,true,Lax", "true,true,Strict", "true,true,None", "false,false,Lax"})
    @DisplayName("설정값과 Duration의 초 단위 만료 시간을 적용하고 기존 응답 헤더를 보존한다")
    void writeRefreshTokenCookie_appliesPropertiesAndPreservesHeaders(
            boolean httpOnly, boolean secure, String sameSite) {
        configureManager(httpOnly, secure, sameSite);
        given(responseProvider.getObject()).willReturn(response);
        addExistingHeaders();
        Duration maxAge = Duration.ofDays(14);

        manager.writeRefreshTokenCookie(TOKEN, maxAge);

        assertRefreshCookie(TOKEN, maxAge.getSeconds());
        assertExistingHeadersPreserved();
        verifyNoInteractions(requestProvider);
    }

    @ParameterizedTest
    @CsvSource({"true,false,Lax", "true,true,Lax", "true,true,Strict", "true,true,None", "false,false,Lax"})
    @DisplayName("요청 객체 없이도 같은 설정의 빈 값과 Max-Age 0인 삭제 쿠키를 추가한다")
    void clearRefreshTokenCookie_expiresCookieWithoutRequestLookup(
            boolean httpOnly, boolean secure, String sameSite) {
        configureManager(httpOnly, secure, sameSite);
        given(responseProvider.getObject()).willReturn(response);
        addExistingHeaders();

        manager.clearRefreshTokenCookie();

        assertRefreshCookie("", 0);
        assertExistingHeadersPreserved();
        verifyNoInteractions(requestProvider);
    }

    @Test
    @DisplayName("쿠키를 읽은 뒤 삭제해도 기존 요청 쿠키는 변경하지 않는다")
    void clearRefreshTokenCookie_doesNotMutatePreviouslyReadCookie() {
        Cookie refreshCookie = createRequestCookie();
        request.setCookies(refreshCookie);
        given(requestProvider.getObject()).willReturn(request);
        given(responseProvider.getObject()).willReturn(response);
        assertThat(manager.readRefreshToken()).contains(TOKEN);

        manager.clearRefreshTokenCookie();

        assertRequestCookieUnchanged(refreshCookie);
        assertRefreshCookie("", 0);
    }

    private void configureManager(boolean httpOnly, boolean secure, String sameSite) {
        properties = new RefreshCookieProperties(httpOnly, secure, sameSite);
        manager = new RefreshTokenCookieManagerImpl(responseProvider, requestProvider, properties);
    }

    private Cookie createRequestCookie() {
        Cookie cookie = new Cookie(COOKIE_NAME, TOKEN);
        cookie.setMaxAge(600);
        cookie.setPath("/api/auth");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }

    private void assertRequestCookieUnchanged(Cookie cookie) {
        assertThat(cookie.getValue()).isEqualTo(TOKEN);
        assertThat(cookie.getMaxAge()).isEqualTo(600);
        assertThat(cookie.getPath()).isEqualTo("/api/auth");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
    }

    private void addExistingHeaders() {
        response.addHeader(HttpHeaders.SET_COOKIE, OTHER_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, CSRF_COOKIE);
        response.addHeader("X-Request-Id", "request-id");
    }

    private void assertExistingHeadersPreserved() {
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(3)
                .contains(OTHER_COOKIE, CSRF_COOKIE);
        assertThat(response.getHeader("X-Request-Id")).isEqualTo("request-id");
        assertThat(response.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
    }

    private void assertRefreshCookie(String value, long maxAgeSeconds) {
        List<String> refreshHeaders = response.getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(header -> header.startsWith(COOKIE_NAME + "="))
                .toList();
        assertThat(refreshHeaders).hasSize(1);
        String header = refreshHeaders.get(0);
        HttpCookie cookie = HttpCookie.parse(header).get(0);
        assertThat(cookie.getName()).isEqualTo(COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(value);
        assertThat(cookie.getMaxAge()).isEqualTo(maxAgeSeconds);
        assertThat(cookie.isHttpOnly()).isEqualTo(properties.httpOnly());
        assertThat(cookie.getSecure()).isEqualTo(properties.secure());
        assertThat(header).contains("SameSite=" + properties.sameSite());
        assertThat(cookie.getPath()).isNull();
        assertThat(cookie.getDomain()).isNull();
    }
}
