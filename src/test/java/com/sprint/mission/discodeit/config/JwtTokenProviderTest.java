package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.config.JwtTokenProvider.TokenType;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String USERNAME = "test-user";
    private static final String SECRET = "jwt-test-secret-with-at-least-32-bytes";
    private static final String ISSUER = "discodeit-test";
    private static final Duration ACCESS_VALIDITY = Duration.ofMinutes(10);
    private static final Duration REFRESH_VALIDITY = Duration.ofDays(14);
    private static final Instant ISSUED_AT = Instant.parse("2026-09-21T00:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(ISSUED_AT, ZoneOffset.UTC);
    private static final SecretKey SIGNING_KEY =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final JwtProperties PROPERTIES =
            new JwtProperties(SECRET, ACCESS_VALIDITY, REFRESH_VALIDITY, ISSUER);

    @Mock
    private DiscodeitUserDetailsService userDetailsService;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(PROPERTIES, FIXED_CLOCK, userDetailsService);
    }

    @Test
    @DisplayName("Access Token에 사용자 정보와 권한, 발급 정보, 만료기간을 담아 HS256으로 서명한다")
    void generateAccessToken_signsUserClaimsWithAccessValidity() {
        String token = provider.generateAccessToken(USER_ID, USERNAME, Role.ADMIN);

        Jws<Claims> jwt = readSignedToken(token, FIXED_CLOCK);
        Claims claims = jwt.getPayload();
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(claims)
                .containsEntry("user_id", USER_ID.toString())
                .containsEntry("role", "ADMIN")
                .containsEntry("token_type", "ACCESS");
        assertThat(claims.getSubject()).isEqualTo(USERNAME);
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(ISSUED_AT);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(ISSUED_AT.plus(ACCESS_VALIDITY));
    }

    @Test
    @DisplayName("Refresh Token은 권한 없이 사용자 정보와 발급 정보, 별도의 만료기간을 담는다")
    void generateRefreshToken_signsUserClaimsWithoutRoleWithRefreshValidity() {
        String token = provider.generateRefreshToken(USER_ID, USERNAME);

        Jws<Claims> jwt = readSignedToken(token, FIXED_CLOCK);
        Claims claims = jwt.getPayload();
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(claims)
                .containsEntry("user_id", USER_ID.toString())
                .containsEntry("token_type", "REFRESH")
                .doesNotContainKey("role");
        assertThat(claims.getSubject()).isEqualTo(USERNAME);
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(ISSUED_AT);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(ISSUED_AT.plus(REFRESH_VALIDITY));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(TokenType.class)
    @DisplayName("정상 토큰을 검증하고 JSON 문자열로 저장된 사용자 ID를 UUID로 복원한다")
    void validateToken_returnsClaimsAndRestoresUserId(TokenType type) {
        String token = signedToken(type.name(), ISSUER, SIGNING_KEY, ISSUED_AT.plusSeconds(600));

        assertThat(provider.validateToken(token)).hasValueSatisfying(claims -> {
            assertThat(claims.getSubject()).isEqualTo(USERNAME);
            assertThat(claims).containsEntry("token_type", type.name());
            assertThat(provider.getUserId(claims)).isEqualTo(USER_ID);
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    @DisplayName("유효하지 않은 토큰은 예외를 전파하지 않고 빈 결과를 반환한다")
    void validateToken_returnsEmptyForInvalidToken(String scenario, String token) {
        assertThat(provider.validateToken(token)).isEmpty();
    }

    @Test
    @DisplayName("Refresh Token으로 현재 사용자 권한과 갱신 시각을 반영한 토큰 쌍을 발급한다")
    void renewGenerateToken_usesCurrentRoleAndNewExpirationTimes() {
        String refreshToken = provider.generateRefreshToken(USER_ID, USERNAME);
        Clock renewalClock = Clock.offset(FIXED_CLOCK, Duration.ofHours(1));
        JwtTokenProvider renewalProvider =
                new JwtTokenProvider(PROPERTIES, renewalClock, userDetailsService);
        UserDto currentUser = userDto(Role.CHANNEL_MANAGER);
        given(userDetailsService.loadUserByUsername(USERNAME))
                .willReturn(new DiscodeitUserDetails(currentUser, "unused-password"));

        TokenDto result = renewalProvider.renewGenerateToken(refreshToken);

        Claims access = readSignedToken(result.accessToken(), renewalClock).getPayload();
        Claims refresh = readSignedToken(result.refreshToken(), renewalClock).getPayload();
        assertThat(access)
                .containsEntry("user_id", USER_ID.toString())
                .containsEntry("token_type", "ACCESS")
                .containsEntry("role", "CHANNEL_MANAGER");
        assertThat(refresh)
                .containsEntry("user_id", USER_ID.toString())
                .containsEntry("token_type", "REFRESH")
                .doesNotContainKey("role");
        assertThat(access.getSubject()).isEqualTo(currentUser.username());
        assertThat(refresh.getSubject()).isEqualTo(currentUser.username());
        assertThat(access.getIssuer()).isEqualTo(ISSUER);
        assertThat(refresh.getIssuer()).isEqualTo(ISSUER);
        assertThat(access.getIssuedAt().toInstant()).isEqualTo(renewalClock.instant());
        assertThat(refresh.getIssuedAt().toInstant()).isEqualTo(renewalClock.instant());
        assertThat(access.getExpiration().toInstant())
                .isEqualTo(renewalClock.instant().plus(ACCESS_VALIDITY));
        assertThat(refresh.getExpiration().toInstant())
                .isEqualTo(renewalClock.instant().plus(REFRESH_VALIDITY));
        then(userDetailsService).should().loadUserByUsername(USERNAME);
    }

    @Test
    @DisplayName("Access Token으로 갱신하면 사용자 조회 전에 전용 예외를 발생시킨다")
    void renewGenerateToken_rejectsAccessTokenBeforeUserLookup() {
        String token = provider.generateAccessToken(USER_ID, USERNAME, Role.USER);

        assertThatThrownBy(() -> provider.renewGenerateToken(token))
                .isInstanceOf(TokenRenewalFailedException.class)
                .hasMessage("토큰 갱신에 실패했습니다.");
        verifyNoInteractions(userDetailsService);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    @DisplayName("유효하지 않은 토큰으로 갱신하면 사용자 조회 전에 전용 예외를 발생시킨다")
    void renewGenerateToken_rejectsInvalidTokenBeforeUserLookup(String scenario, String token) {
        assertThatThrownBy(() -> provider.renewGenerateToken(token))
                .isInstanceOf(TokenRenewalFailedException.class)
                .hasMessage("토큰 갱신에 실패했습니다.");
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("사용자 조회 실패는 기존 사용자 조회 예외를 그대로 전파한다")
    void renewGenerateToken_propagatesUserLookupFailure() {
        String token = provider.generateRefreshToken(USER_ID, USERNAME);
        UsernameNotFoundException failure = new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        given(userDetailsService.loadUserByUsername(USERNAME)).willThrow(failure);

        assertThatThrownBy(() -> provider.renewGenerateToken(token)).isSameAs(failure);
    }

    private static Stream<Arguments> invalidTokens() {
        Instant validExpiration = ISSUED_AT.plus(REFRESH_VALIDITY);
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "another-test-secret-with-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        return Stream.of(
                Arguments.of("만료된 토큰",
                        signedToken("REFRESH", ISSUER, SIGNING_KEY, ISSUED_AT.minusSeconds(1))),
                Arguments.of("서명 키 불일치",
                        signedToken("REFRESH", ISSUER, otherKey, validExpiration)),
                Arguments.of("서명 후 Payload 변조", tamperedToken()),
                Arguments.of("발급자 불일치",
                        signedToken("REFRESH", "another-issuer", SIGNING_KEY, validExpiration)),
                Arguments.of("타입 클레임 누락",
                        signedToken(null, ISSUER, SIGNING_KEY, validExpiration)),
                Arguments.of("알 수 없는 토큰 타입",
                        signedToken("UNKNOWN", ISSUER, SIGNING_KEY, validExpiration)),
                Arguments.of("잘못된 토큰 형식", "not-a-jwt"),
                Arguments.of("null 입력", (String) null),
                Arguments.of("빈 입력", ""),
                Arguments.of("공백 입력", " ")
        );
    }

    private static String signedToken(String type, String issuer, SecretKey key, Instant expiration) {
        return Jwts.builder()
                .subject(USERNAME)
                .claim("user_id", USER_ID.toString())
                .claim("token_type", type)
                .claim("role", "ACCESS".equals(type) ? "USER" : null)
                .issuer(issuer)
                .issuedAt(Date.from(ISSUED_AT.minusSeconds(60)))
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private static String tamperedToken() {
        String token = signedToken("REFRESH", ISSUER, SIGNING_KEY, ISSUED_AT.plus(REFRESH_VALIDITY));
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String tamperedPayload = payload.replace(
                USER_ID.toString(), "00000000-0000-0000-0000-000000000002");
        parts[1] = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(tamperedPayload.getBytes(StandardCharsets.UTF_8));
        return String.join(".", parts);
    }

    private static Jws<Claims> readSignedToken(String token, Clock clock) {
        return Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token);
    }

    private static UserDto userDto(Role role) {
        OffsetDateTime time = OffsetDateTime.ofInstant(ISSUED_AT, ZoneOffset.UTC);
        return new UserDto(USER_ID, USERNAME, "test@example.com", null, true, role, time, time);
    }
}
