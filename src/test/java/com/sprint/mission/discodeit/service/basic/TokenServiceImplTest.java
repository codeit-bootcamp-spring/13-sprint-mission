package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.ArgumentMatchers.anyString;
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
    RefreshTokenCookieManager cookieManager;

    TokenServiceImpl service;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        service = new TokenServiceImpl(jwtTokenProvider, userDetailsService, cookieManager);
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
        verifyNoInteractions(userDetailsService, cookieManager);
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
        verify(cookieManager).readRefreshToken();
        verify(userDetailsService).loadUserByUsername(userDto.username());
        verify(jwtTokenProvider).generateAccessToken(userDto.id(), userDto.username(), userDto.role());
        verify(jwtTokenProvider).generateRefreshToken(userDto.id(), userDto.username());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
        verifyNoCookieChanges();
    }

    @Test
    @DisplayName("검증 실패 토큰은 사용자 조회와 발급 없이 갱신 실패로 처리하고 기존 인증을 유지한다")
    void rotateRefreshToken_rejectsInvalidToken() {
        Authentication previous = setPreviousAuthentication();
        given(cookieManager.readRefreshToken()).willReturn(Optional.of("invalid-token"));
        given(jwtTokenProvider.validateToken("invalid-token")).willReturn(Optional.empty());

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class);

        verify(jwtTokenProvider).validateToken("invalid-token");
        verifyNoInteractions(userDetailsService);
        verifyNoTokenGeneration();
        verifyNoCookieChanges();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
    }

    @Test
    @DisplayName("매니저가 토큰을 반환하지 않으면 JWT 검증 전 갱신 실패로 처리한다")
    void rotateRefreshToken_rejectsMissingRefreshTokenBeforeValidation() {
        Authentication previous = setPreviousAuthentication();
        given(cookieManager.readRefreshToken()).willReturn(Optional.empty());

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class)
                .hasMessage("토큰 갱신에 실패했습니다.");

        verify(cookieManager).readRefreshToken();
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
        verifyNoCookieChanges();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
    }

    @Test
    @DisplayName("정상 ACCESS 토큰도 사용자 조회 전에 거부한다")
    void rotateRefreshToken_rejectsAccessToken() {
        stubValidatedToken(JwtTokenProvider.TokenType.ACCESS);

        assertThatThrownBy(service::rotateRefreshToken)
                .isInstanceOf(TokenRenewalFailedException.class);

        verifyNoInteractions(userDetailsService);
        verifyNoTokenGeneration();
        verifyNoCookieChanges();
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
        verifyNoCookieChanges();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(previous);
    }

    @Test
    @DisplayName("리프레시 토큰과 Provider가 반환한 Duration을 그대로 전달해 쿠키 쓰기를 위임한다")
    void addRefreshTokenCookie_delegatesTokenAndLifetime() {
        Duration lifetime = Duration.ofDays(14);
        given(jwtTokenProvider.getRefreshTokenExpirationTime()).willReturn(lifetime);

        service.addRefreshTokenCookie(NEW_REFRESH_TOKEN);

        verify(cookieManager).writeRefreshTokenCookie(NEW_REFRESH_TOKEN, lifetime);
        verify(cookieManager, never()).readRefreshToken();
        verify(cookieManager, never()).clearRefreshTokenCookie();
        verify(jwtTokenProvider, never()).validateToken(any());
        verifyNoInteractions(userDetailsService);
    }

    private void stubGeneratedTokens() {
        given(jwtTokenProvider.generateAccessToken(userDto.id(), userDto.username(), userDto.role()))
                .willReturn(NEW_ACCESS_TOKEN);
        given(jwtTokenProvider.generateRefreshToken(userDto.id(), userDto.username()))
                .willReturn(NEW_REFRESH_TOKEN);
    }

    private void stubValidatedToken(JwtTokenProvider.TokenType type) {
        given(cookieManager.readRefreshToken()).willReturn(Optional.of(REFRESH_TOKEN));
        Claims claims = Jwts.claims().subject(userDto.username())
                .add(JwtTokenProvider.CLAIM_TOKEN_TYPE, type.name()).build();
        given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(Optional.of(claims));
        given(jwtTokenProvider.getTokenType(claims)).willReturn(type.name());
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

    private void verifyNoCookieChanges() {
        verify(cookieManager, never()).writeRefreshTokenCookie(anyString(), any(Duration.class));
        verify(cookieManager, never()).clearRefreshTokenCookie();
    }
}
