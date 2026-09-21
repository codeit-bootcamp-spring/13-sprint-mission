package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenServiceImpl 단위 테스트")
class TokenServiceImplTest {

    private static final String REFRESH_TOKEN = "existing-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    DiscodeitUserDetailsService userDetailsService;

    @Mock
    ObjectProvider<HttpServletResponse> responseProvider;

    @Mock
    ObjectProvider<HttpServletRequest> requestProvider;

    TokenServiceImpl service;
    UserDto userDto;
    MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        service = new TokenServiceImpl(jwtTokenProvider,
                new RefreshCookieProperties(true, false, "Lax"), userDetailsService, responseProvider, requestProvider);
        request = new MockHttpServletRequest("POST", "/api/auth/refresh");
        OffsetDateTime now = OffsetDateTime.parse("2026-09-21T00:00:00Z");
        userDto = new UserDto(UUID.randomUUID(), "refresh-user", "refresh@example.com",
                null, true, Role.CHANNEL_MANAGER, now, now);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("현재 사용자 ID와 사용자명, 역할로 두 종류의 토큰을 발급한다")
    void generateToken_usesCurrentUserInformation() {
        stubGeneratedTokens();

        TokenDto result = service.generateToken(userDto);

        assertThat(result).isEqualTo(new TokenDto(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN));
        verify(jwtTokenProvider).generateAccessToken(userDto.id(), userDto.username(), userDto.role());
        verify(jwtTokenProvider).generateRefreshToken(userDto.id(), userDto.username());
        verifyNoInteractions(userDetailsService, responseProvider, requestProvider);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("갱신은 현재 사용자 정보를 직접 반환하며 기존 인증 유무와 관계없이 컨텍스트를 변경하지 않는다")
    void rotateRefreshToken_returnsCurrentUserWithoutChangingAuthentication(boolean alreadyAuthenticated) {
        Authentication previous = alreadyAuthenticated ? setPreviousAuthentication() : null;
        stubValidatedToken(JwtTokenProvider.TokenType.REFRESH);
        given(userDetailsService.loadUserByUsername(userDto.username()))
                .willReturn(new DiscodeitUserDetails(userDto, "unused-password"));
        stubGeneratedTokens();

        JwtDtoWithRefresh result = service.rotateRefreshToken();

        assertThat(result).isEqualTo(new JwtDtoWithRefresh(
                new JwtDto(userDto, NEW_ACCESS_TOKEN), NEW_REFRESH_TOKEN));
        verify(userDetailsService).loadUserByUsername(userDto.username());
        verify(jwtTokenProvider).generateAccessToken(userDto.id(), userDto.username(), userDto.role());
        verify(jwtTokenProvider).generateRefreshToken(userDto.id(), userDto.username());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
        verifyNoInteractions(responseProvider);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "invalid-token"})
    @DisplayName("검증 실패 토큰은 사용자 조회와 발급 없이 갱신 실패로 처리하고 기존 인증을 유지한다")
    void rotateRefreshToken_rejectsInvalidToken(String token) {
        Authentication previous = setPreviousAuthentication();
        stubRequestWithRefreshToken(token);
        given(jwtTokenProvider.validateToken(token)).willReturn(Optional.empty());

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class);

        verify(jwtTokenProvider).validateToken(token);
        verifyNoInteractions(userDetailsService, responseProvider);
        verifyNoTokenGeneration();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "empty", "unrelated"})
    @DisplayName("리프레시 쿠키가 없으면 사용자 조회와 토큰 발급 없이 갱신 실패로 처리한다")
    void rotateRefreshToken_rejectsMissingRefreshCookie(String cookies) {
        Authentication previous = setPreviousAuthentication();
        if ("empty".equals(cookies)) {
            request.setCookies(new Cookie[0]);
        } else if ("unrelated".equals(cookies)) {
            request.setCookies(new Cookie("XSRF-TOKEN", "csrf"));
        }
        given(requestProvider.getObject()).willReturn(request);
        given(jwtTokenProvider.validateToken(null)).willReturn(Optional.empty());

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class)
                .hasMessage("토큰 갱신에 실패했습니다.");

        verify(jwtTokenProvider).validateToken(null);
        verifyNoInteractions(userDetailsService, responseProvider);
        verifyNoTokenGeneration();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
    }

    @Test
    @DisplayName("정상 ACCESS 토큰도 사용자 조회 전에 거부한다")
    void rotateRefreshToken_rejectsAccessToken() {
        stubValidatedToken(JwtTokenProvider.TokenType.ACCESS);

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class);

        verifyNoInteractions(userDetailsService, responseProvider);
        verifyNoTokenGeneration();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("사용자 조회 실패는 갱신 실패 예외로 변환하고 토큰 발급과 인증 변경을 하지 않는다")
    void rotateRefreshToken_convertsMissingUserToRenewalFailure() {
        Authentication previous = setPreviousAuthentication();
        stubValidatedToken(JwtTokenProvider.TokenType.REFRESH);
        given(userDetailsService.loadUserByUsername(userDto.username()))
                .willThrow(new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class)
                .hasMessage("토큰 갱신에 실패했습니다.");

        verifyNoTokenGeneration();
        verifyNoInteractions(responseProvider);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
    }

    @ParameterizedTest(name = "HttpOnly={0}, Secure={1}, SameSite={2}")
    @CsvSource({
            "true, false, Lax",
            "true, true, Lax",
            "true, true, Strict",
            "true, true, None",
            "false, false, Lax"
    })
    @DisplayName("설정된 쿠키 속성과 만료 시간을 적용하고 기존 Set-Cookie 헤더를 보존한다")
    void addRefreshTokenCookie_usesPropertiesAndPreservesOtherCookies(
            boolean httpOnly, boolean secure, String sameSite
    ) {
        service = new TokenServiceImpl(jwtTokenProvider,
                new RefreshCookieProperties(httpOnly, secure, sameSite), userDetailsService, responseProvider, requestProvider);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String existingCookie = "XSRF-TOKEN=existing-csrf; Path=/";
        response.addHeader(HttpHeaders.SET_COOKIE, existingCookie);
        long lifetime = Duration.ofDays(14).getSeconds();
        given(responseProvider.getObject()).willReturn(response);
        given(jwtTokenProvider.getRefreshTokenLifetime()).willReturn(lifetime);

        service.addRefreshTokenCookie(NEW_REFRESH_TOKEN);

        Cookie cookie = response.getCookie("REFRESH_TOKEN");
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEqualTo(NEW_REFRESH_TOKEN);
        assertThat(cookie.isHttpOnly()).isEqualTo(httpOnly);
        assertThat(cookie.getSecure()).isEqualTo(secure);
        assertThat(cookie.getAttribute("SameSite")).isEqualTo(sameSite);
        assertThat(cookie.getMaxAge()).isEqualTo(Math.toIntExact(lifetime));
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE)).hasSize(2).contains(existingCookie);
    }

    private void stubGeneratedTokens() {
        given(jwtTokenProvider.generateAccessToken(userDto.id(), userDto.username(), userDto.role()))
                .willReturn(NEW_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(userDto.id(), userDto.username()))
                .willReturn(NEW_REFRESH_TOKEN);
    }

    private void stubValidatedToken(JwtTokenProvider.TokenType type) {
        stubRequestWithRefreshToken(REFRESH_TOKEN);
        Claims claims = Jwts.claims().subject(userDto.username())
                .add(JwtTokenProvider.CLAIM_TOKEN_TYPE, type.name()).build();
        given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(type.name());
    }

    private void stubRequestWithRefreshToken(String token) {
        request.setCookies(new Cookie("XSRF-TOKEN", "csrf"), new Cookie("REFRESH_TOKEN", token));
        given(requestProvider.getObject()).willReturn(request);
    }

    private Authentication setPreviousAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("another-user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private void verifyNoTokenGeneration() {
        verify(jwtTokenProvider, never()).generateAccessToken(any(), any(), any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any(), any());
    }
}
