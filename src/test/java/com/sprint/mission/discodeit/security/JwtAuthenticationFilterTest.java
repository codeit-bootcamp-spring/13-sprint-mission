package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 단위 테스트")
class JwtAuthenticationFilterTest {

    private static final String ACCESS_TOKEN = "valid-access-token";
    private static final String REFRESH_TOKEN = "valid-refresh-token";
    private static final String USERNAME = "jwt-test-user";

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    DiscodeitUserDetailsService userDetailsService;

    @Mock
    AuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    FilterChain filterChain;

    @Mock
    JwtRegistry jwtRegistry;

    JwtAuthenticationFilter filter;
    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, authenticationEntryPoint, jwtRegistry);
        request = new MockHttpServletRequest("GET", "/api/users");
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Basic credentials", "Bearer"})
    @DisplayName("Bearer 헤더가 없으면 인증을 시도하지 않고 다음 필터로 진행한다")
    void doFilter_skipsAuthenticationWithoutBearerHeader(String header) throws Exception {
        if (header != null) {
            request.addHeader(HttpHeaders.AUTHORIZATION, header);
        }

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider, jwtRegistry, userDetailsService, authenticationEntryPoint);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("검증된 ACCESS 토큰의 사용자와 권한으로 인증 완료 상태를 등록한다")
    void doFilter_authenticatesWithValidAccessToken() throws Exception {
        DiscodeitUserDetails userDetails = stubAccessAuthentication();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(userDetails);
        assertThat(authentication.getName()).isEqualTo(USERNAME);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        verify(jwtTokenProvider).validateToken(ACCESS_TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationEntryPoint);
    }

    @Test
    @DisplayName("유효한 REFRESH 토큰도 사용자 조회나 인증에 사용하지 않는다")
    void doFilter_rejectsRefreshTokenBeforeUserLookup() throws Exception {
        stubValidatedToken(REFRESH_TOKEN, JwtTokenProvider.TokenType.REFRESH);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + REFRESH_TOKEN);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider).validateToken(REFRESH_TOKEN);
        verifyNoInteractions(userDetailsService, authenticationEntryPoint);
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid-token"})
    @DisplayName("빈 토큰이나 검증 실패 결과는 인증을 설정하지 않고 다음 필터로 전달한다")
    void doFilter_continuesWithoutAuthenticationWhenValidationReturnsEmpty(String token) throws Exception {
        given(jwtTokenProvider.validateToken(token)).willReturn(Optional.empty());
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider).validateToken(token);
        verifyNoInteractions(userDetailsService, authenticationEntryPoint);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("사용자 조회 실패 시 기존 인증을 정리하고 EntryPoint로 위임한 뒤 요청을 중단한다")
    void doFilter_clearsContextAndStopsChainWhenUserIsMissing() throws Exception {
        stubValidatedToken(ACCESS_TOKEN, JwtTokenProvider.TokenType.ACCESS);
        given(jwtRegistry.hasActiveJwtInformationByAccessToken(ACCESS_TOKEN)).willReturn(true);
        UsernameNotFoundException failure = new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        given(userDetailsService.loadUserByUsername(USERNAME)).willThrow(failure);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previous-user", null, List.of())
        );
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(authenticationEntryPoint).commence(request, response, failure);
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("뒤쪽 필터의 인증 예외는 JWT 인증 실패로 처리하지 않고 그대로 전파한다")
    void doFilter_propagatesDownstreamAuthenticationException() throws Exception {
        stubAccessAuthentication();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        BadCredentialsException failure = new BadCredentialsException("뒤쪽 필터에서 발생한 인증 예외");
        doThrow(failure).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain)).isSameAs(failure);

        verifyNoInteractions(authenticationEntryPoint);
    }

    @Test
    @DisplayName("동일 요청에서 필터를 중첩 호출해도 토큰 검증과 사용자 조회는 한 번만 수행한다")
    void doFilter_authenticatesOnlyOnceDuringNestedInvocation() throws Exception {
        DiscodeitUserDetails userDetails = stubAccessAuthentication();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);
        // 요청 처리가 끝난 뒤 재호출하는 것이 아니라, 같은 체인 안에서의 중복 진입을 재현한다.
        FilterChain reenteringChain = (nestedRequest, nestedResponse) ->
                filter.doFilter(nestedRequest, nestedResponse, filterChain);

        filter.doFilter(request, response, reenteringChain);

        verify(jwtTokenProvider).validateToken(ACCESS_TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(userDetails);
    }

    @Test
    @DisplayName("서블릿 등록 방지 빈은 동일한 JWT 필터 인스턴스를 비활성화 대상으로 지정한다")
    void registration_disablesServletRegistrationOfSameFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                filter.discodeitAuthenticationFilterRegistration();

        assertThat(registration.getFilter()).isSameAs(filter);
        assertThat(registration.isEnabled()).isFalse();
    }

    private void stubValidatedToken(String token, JwtTokenProvider.TokenType type) {
        Claims claims = Jwts.claims()
                .subject(USERNAME)
                .add("token_type", type.name())
                .build();
        given(jwtTokenProvider.validateToken(token)).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(type.name());
    }

    private DiscodeitUserDetails stubAccessAuthentication() {
        stubValidatedToken(ACCESS_TOKEN, JwtTokenProvider.TokenType.ACCESS);
        given(jwtRegistry.hasActiveJwtInformationByAccessToken(ACCESS_TOKEN)).willReturn(true);
        OffsetDateTime now = OffsetDateTime.parse("2026-09-21T00:00:00Z");
        UserDto userDto = new UserDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                USERNAME, "jwt-test@example.com", null, true, Role.USER, now, now
        );
        DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userDto, "unused-password");
        given(userDetailsService.loadUserByUsername(USERNAME)).willReturn(userDetails);
        return userDetails;
    }

    @Test
    @DisplayName("서명과 만료가 유효해도 레지스트리에 없는 ACCESS 토큰으로 인증하지 않는다")
    void doFilter_rejectsUnregisteredAccessToken() throws Exception {
        stubValidatedToken(ACCESS_TOKEN, JwtTokenProvider.TokenType.ACCESS);
        given(jwtRegistry.hasActiveJwtInformationByAccessToken(ACCESS_TOKEN)).willReturn(false);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService, authenticationEntryPoint);
        verify(filterChain).doFilter(request, response);
    }
}
