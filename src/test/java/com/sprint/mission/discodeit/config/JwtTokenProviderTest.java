package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.JwtTokenProvider.TokenType;
import com.sprint.mission.discodeit.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(PROPERTIES, FIXED_CLOCK);
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
            assertThat(provider.getTokenType(claims)).isEqualTo(type.name());
            assertThat(provider.getUserId(claims)).isEqualTo(USER_ID);
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    @DisplayName("유효하지 않은 토큰은 예외를 전파하지 않고 빈 결과를 반환한다")
    void validateToken_returnsEmptyForInvalidToken(String scenario, String token) {
        assertThat(provider.validateToken(token)).isEmpty();
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
}
