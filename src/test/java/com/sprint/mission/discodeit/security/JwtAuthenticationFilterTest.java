package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private DiscodeitUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(
                        jwtTokenProvider,
                        jwtRegistry,
                        userDetailsService
                );

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("JWT 인증")
    class AuthenticationTest {

        @Test
        @DisplayName("Authorization 헤더가 없으면 인증하지 않고 다음 필터로 진행")
        void doFilter_noAuthorizationHeader() throws Exception {
            // given
            MockHttpServletRequest request =
                    new MockHttpServletRequest();

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            // when
            jwtAuthenticationFilter.doFilter(
                    request,
                    response,
                    filterChain
            );

            // then
            assertThat(
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
            ).isNull();

            then(filterChain).should()
                    .doFilter(request, response);

            then(jwtTokenProvider)
                    .shouldHaveNoInteractions();

            then(jwtRegistry)
                    .shouldHaveNoInteractions();

            then(userDetailsService)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("유효하고 활성 상태인 Access Token이면 인증 정보를 등록")
        void doFilter_validAndActiveToken() throws Exception {
            // given
            String token = "valid-access-token";
            String username = "user1";

            UserDto userDto = new UserDto(
                    UUID.randomUUID(),
                    username,
                    "user1@test.com",
                    null,
                    true,
                    Role.USER
            );

            DiscodeitUserDetails userDetails =
                    new DiscodeitUserDetails(
                            userDto,
                            "encoded-password"
                    );

            MockHttpServletRequest request =
                    new MockHttpServletRequest();

            request.addHeader(
                    "Authorization",
                    "Bearer " + token
            );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            given(jwtTokenProvider.isValid(token))
                    .willReturn(true);

            given(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(token)
            ).willReturn(true);

            given(jwtTokenProvider.getUsername(token))
                    .willReturn(username);

            given(userDetailsService.loadUserByUsername(username))
                    .willReturn(userDetails);

            // when
            jwtAuthenticationFilter.doFilter(
                    request,
                    response,
                    filterChain
            );

            // then
            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            assertThat(authentication)
                    .isNotNull();

            assertThat(authentication.isAuthenticated())
                    .isTrue();

            assertThat(authentication.getPrincipal())
                    .isEqualTo(userDetails);

            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactlyElementsOf(
                            userDetails.getAuthorities()
                                    .stream()
                                    .map(authority -> authority.getAuthority())
                                    .toList()
                    );

            then(jwtTokenProvider).should()
                    .isValid(token);

            then(jwtRegistry).should()
                    .hasActiveJwtInformationByAccessToken(token);

            then(jwtTokenProvider).should()
                    .getUsername(token);

            then(userDetailsService).should()
                    .loadUserByUsername(username);

            then(filterChain).should()
                    .doFilter(request, response);
        }

        @Test
        @DisplayName("JWT는 유효하지만 Registry에 없으면 인증하지 않음")
        void doFilter_validButInactiveToken() throws Exception {
            // given
            String token = "inactive-access-token";

            MockHttpServletRequest request =
                    new MockHttpServletRequest();

            request.addHeader(
                    "Authorization",
                    "Bearer " + token
            );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            given(jwtTokenProvider.isValid(token))
                    .willReturn(true);

            given(
                    jwtRegistry
                            .hasActiveJwtInformationByAccessToken(token)
            ).willReturn(false);

            // when
            jwtAuthenticationFilter.doFilter(
                    request,
                    response,
                    filterChain
            );

            // then
            assertThat(
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
            ).isNull();

            then(jwtTokenProvider).should()
                    .isValid(token);

            then(jwtRegistry).should()
                    .hasActiveJwtInformationByAccessToken(token);

            then(jwtTokenProvider).should(never())
                    .getUsername(token);

            then(userDetailsService)
                    .shouldHaveNoInteractions();

            then(filterChain).should()
                    .doFilter(request, response);
        }

        @Test
        @DisplayName("유효하지 않은 JWT이면 인증하지 않음")
        void doFilter_invalidToken() throws Exception {
            // given
            String token = "invalid-access-token";

            MockHttpServletRequest request =
                    new MockHttpServletRequest();

            request.addHeader(
                    "Authorization",
                    "Bearer " + token
            );

            MockHttpServletResponse response =
                    new MockHttpServletResponse();

            given(jwtTokenProvider.isValid(token))
                    .willReturn(false);

            // when
            jwtAuthenticationFilter.doFilter(
                    request,
                    response,
                    filterChain
            );

            // then
            assertThat(
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication()
            ).isNull();

            then(jwtTokenProvider).should()
                    .isValid(token);

            then(jwtRegistry)
                    .shouldHaveNoInteractions();

            then(jwtTokenProvider).should(never())
                    .getUsername(token);

            then(userDetailsService)
                    .shouldHaveNoInteractions();

            then(filterChain).should()
                    .doFilter(request, response);
        }
    }
}