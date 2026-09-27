package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:jwt-authentication-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "discodeit.storage.type=local",
        "discodeit.storage.local.root-path=./build/test-uploads/jwt-authentication",
        "discodeit.security.jwt.secret=jwt-authentication-integration-test-secret-with-at-least-32-bytes",
        "discodeit.security.jwt.access-token-validity=10m",
        "discodeit.security.jwt.refresh-token-validity=14d",
        "discodeit.security.jwt.issuer=jwt-authentication-test",
        "discodeit.security.refresh-cookie.http-only=true",
        "discodeit.security.refresh-cookie.secure=false",
        "discodeit.security.refresh-cookie.same-site=Lax"
})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("실제 서버의 JWT 인증 통합 테스트")
class JwtAuthenticationIntegrationTest {

    private static final String RAW_PASSWORD = "jwt-integration-password";

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    JwtRegistry jwtRegistry;

    @Autowired
    UserMapper userMapper;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    RefreshCookieProperties refreshCookieProperties;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    SecurityFilterChain securityFilterChain;

    @Autowired
    ServletContext servletContext;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final List<UUID> createdUserIds = new ArrayList<>();

    @AfterEach
    void deleteCreatedUsers() {
        createdUserIds.forEach(jwtRegistry::invalidateJwtInformationByUserId);
        // 실제 HTTP 요청은 별도 트랜잭션이므로 테스트 롤백 대신 생성한 사용자만 정리한다.
        userRepository.deleteAllById(createdUserIds);
    }

    @Test
    @DisplayName("실제 폼 로그인에서 발급받은 ACCESS 토큰으로 보호된 사용자 목록을 조회한다")
    void getUsers_authenticatesUsingAccessTokenIssuedByFormLogin() throws Exception {
        User user = createUser();
        ResponseEntity<String> loginResponse = login(user);

        ResponseEntity<String> response = getUsers(accessToken(loginResponse));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonContentType(response);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body).anySatisfy(item -> {
            assertThat(item.path("id").asText()).isEqualTo(user.getId().toString());
            assertThat(item.path("username").asText()).isEqualTo(user.getUsername());
            assertThat(item.path("role").asText()).isEqualTo(user.getRole().name());
        });
    }

    @Test
    @DisplayName("토큰 없이 보호 API에 접근하면 401과 AUTH_401 오류를 반환한다")
    void getUsers_returnsUnauthorizedWithoutToken() throws Exception {
        assertUnauthorized(getUsers(null));
    }

    @Test
    @DisplayName("동일한 키로 서명했어도 만료된 ACCESS 토큰은 보호 API에서 거부한다")
    void getUsers_returnsUnauthorizedForExpiredAccessToken() throws Exception {
        User user = createUser();
        // 실행 속도나 대기에 의존하지 않고 과거에 발급되어 만료된 실제 JWT를 만든다.
        JwtTokenProvider pastProvider = new JwtTokenProvider(
                jwtProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
        );
        String expiredToken = pastProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        assertUnauthorized(getUsers(expiredToken));
    }

    @Test
    @DisplayName("잘못된 형식의 토큰으로 보호 API에 접근하면 401을 반환한다")
    void getUsers_returnsUnauthorizedForMalformedToken() throws Exception {
        assertUnauthorized(getUsers("not-a-jwt"));
    }

    @Test
    @DisplayName("로그인에서 발급한 REFRESH 토큰을 Bearer 헤더로 보내도 인증하지 않는다")
    void getUsers_rejectsRefreshTokenFromLogin() throws Exception {
        ResponseEntity<String> loginResponse = login(createUser());
        String refreshToken = cookie(loginResponse, "REFRESH_TOKEN").getValue();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isPresent();

        assertUnauthorized(getUsers(refreshToken));
    }

    @Test
    @DisplayName("JWT 인증 후 토큰을 생략한 다음 요청은 미인증이며 세션 쿠키를 발급하지 않는다")
    void authentication_doesNotPersistToNextRequest() throws Exception {
        ResponseEntity<String> loginResponse = login(createUser());

        ResponseEntity<String> authenticatedResponse = getUsers(accessToken(loginResponse));
        ResponseEntity<String> nextResponse = getUsers(null);

        assertThat(authenticatedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertUnauthorized(nextResponse);
        assertNoSessionCookie(loginResponse);
        assertNoSessionCookie(authenticatedResponse);
        assertNoSessionCookie(nextResponse);
    }

    @Test
    @DisplayName("토큰이 유효해도 사용자가 삭제되었다면 서버 오류 대신 401을 반환한다")
    void getUsers_returnsUnauthorizedAfterUserDeleted() throws Exception {
        User user = createUser();
        String token = accessToken(login(user));
        userRepository.deleteById(user.getId());
        assertThat(jwtTokenProvider.validateToken(token)).isPresent();

        assertUnauthorized(getUsers(token));
    }

    @Test
    @DisplayName("로그인 쿠키만으로 갱신하고 새 ACCESS 토큰으로 인증하며 다음 요청에 인증을 유지하지 않는다")
    void refresh_issuesTokensFromLoginCookieWithoutPersistingAuthentication() throws Exception {
        User user = createUser();
        ResponseEntity<String> loginResponse = login(user);
        assertRefreshCookieAttributes(loginResponse);

        // ACCESS 토큰이나 세션 없이 REFRESH_TOKEN 쿠키와 CSRF 정보만 전송한다.
        ResponseEntity<String> response = refresh(cookie(loginResponse, "REFRESH_TOKEN").getValue());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonContentType(response);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.size()).isEqualTo(2);
        assertThat(body.path("userDto").path("id").asText()).isEqualTo(user.getId().toString());
        assertThat(body.path("userDto").path("username").asText()).isEqualTo(user.getUsername());
        assertThat(body.findValues("refreshToken")).isEmpty();
        assertThat(body.findValues("password")).isEmpty();
        assertTokenClaims(accessToken(response), JwtTokenProvider.TokenType.ACCESS, user);
        assertTokenClaims(cookie(response, "REFRESH_TOKEN").getValue(), JwtTokenProvider.TokenType.REFRESH, user);
        assertRefreshCookieAttributes(response);

        ResponseEntity<String> authenticated = getUsers(accessToken(response));
        assertThat(authenticated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(authenticated.getBody()))
                .anySatisfy(item -> assertThat(item.path("id").asText()).isEqualTo(user.getId().toString()));
        ResponseEntity<String> unauthenticated = getUsers(null);
        assertUnauthorized(unauthenticated);
        assertNoSessionCookie(loginResponse);
        assertNoSessionCookie(response);
        assertNoSessionCookie(authenticated);
        assertNoSessionCookie(unauthenticated);
    }

    @Test
    @DisplayName("만료된 ACCESS 헤더가 있어도 유효한 리프레시 쿠키로 갱신한다")
    void refresh_succeedsWithExpiredAccessToken() throws Exception {
        User user = createUser();
        ResponseEntity<String> loginResponse = login(user);
        JwtTokenProvider pastProvider = new JwtTokenProvider(jwtProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        String expiredAccess = pastProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        assertThat(jwtTokenProvider.validateToken(expiredAccess)).isEmpty();

        ResponseEntity<String> response = refresh(cookie(loginResponse, "REFRESH_TOKEN").getValue(), expiredAccess);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(response.getBody()).path("userDto").path("id").asText())
                .isEqualTo(user.getId().toString());
        assertTokenClaims(accessToken(response), JwtTokenProvider.TokenType.ACCESS, user);
        assertRefreshCookieAttributes(response);
        assertThat(getUsers(accessToken(response)).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNoSessionCookie(response);
    }

    @Test
    @DisplayName("리프레시 쿠키만으로 일반 보호 API를 인증하지 않는다")
    void getUsers_rejectsRefreshCookieWithoutAccessToken() throws Exception {
        String refreshToken = cookie(login(createUser()), "REFRESH_TOKEN").getValue();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.COOKIE, "REFRESH_TOKEN=" + refreshToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertUnauthorized(response);
        assertNoRefreshCookie(response);
        assertNoSessionCookie(response);
    }

    @Test
    @DisplayName("Security 체인에 기존 RememberMe 인증 필터를 등록하지 않는다")
    void securityChain_doesNotContainRememberMeFilter() {
        assertThat(securityFilterChain.getFilters())
                .noneMatch(RememberMeAuthenticationFilter.class::isInstance);
    }

    @Test
    @DisplayName("Me API 매핑을 제거하고 갱신 API의 쿠키·응답 계약을 문서화한다")
    void refresh_isDocumentedAndMeApiIsNotMapped() throws Exception {
        assertThat(requestMappingHandlerMapping.getHandlerMethods().keySet())
                .flatExtracting(info -> info.getPatternValues())
                .doesNotContain("/api/auth/me", "/auth/me");

        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api/docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode paths = objectMapper.readTree(response.getBody()).path("paths");
        assertThat(paths.has("/api/auth/me")).isFalse();
        assertThat(paths.has("/auth/me")).isFalse();
        JsonNode operation = paths.path("/api/auth/refresh").path("post");
        assertThat(operation.isMissingNode()).isFalse();
        assertThat(operation.path("parameters")).anySatisfy(parameter -> {
            assertThat(parameter.path("name").asText()).isEqualTo("REFRESH_TOKEN");
            assertThat(parameter.path("in").asText()).isEqualTo("cookie");
            assertThat(parameter.path("required").asBoolean()).isTrue();
        });
        assertThat(operation.path("parameters")).anySatisfy(parameter -> {
            assertThat(parameter.path("name").asText()).isEqualTo("X-XSRF-TOKEN");
            assertThat(parameter.path("in").asText()).isEqualTo("header");
            assertThat(parameter.path("required").asBoolean()).isTrue();
        });
        JsonNode responses = operation.path("responses");
        assertThat(responses.path("200").path("content").path("application/json").path("schema").path("$ref").asText())
                .isEqualTo("#/components/schemas/JwtDto");
        assertThat(responses.path("200").path("headers").has("Set-Cookie")).isTrue();
        assertThat(responses.path("401").path("content").path("application/json").path("schema").path("$ref").asText())
                .isEqualTo("#/components/schemas/ApiErrorResponse");
        assertThat(responses.has("403")).isTrue();
    }

    @Test
    @DisplayName("권한 변경 API를 거치지 않은 DB 변경도 갱신 시 현재 사용자 정보로 조회한다")
    void refresh_usesCurrentRoleFromDatabase() throws Exception {
        User user = createUser();
        ResponseEntity<String> loginResponse = login(user);
        assertThat(jwtTokenProvider.parseClaims(accessToken(loginResponse)).get("role", String.class))
                .isEqualTo(Role.USER.name());
        user.updateRole(new UserRoleUpdateCommand(Role.CHANNEL_MANAGER));
        userRepository.saveAndFlush(user);

        ResponseEntity<String> response = refresh(cookie(loginResponse, "REFRESH_TOKEN").getValue());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(response.getBody()).path("userDto").path("role").asText())
                .isEqualTo(Role.CHANNEL_MANAGER.name());
        assertTokenClaims(accessToken(response), JwtTokenProvider.TokenType.ACCESS, user);
    }

    @Test
    @DisplayName("과거에 발급한 리프레시 토큰의 갱신은 새 발급 시각과 설정된 유효기간을 적용한다")
    void refresh_renewsIssuedAtAndExpirationWithoutWaiting() throws Exception {
        User user = createUser();
        Instant past = Instant.now().minusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);
        JwtTokenProvider pastProvider = new JwtTokenProvider(jwtProperties, Clock.fixed(past, ZoneOffset.UTC));
        String oldToken = pastProvider.generateRefreshToken(user.getId(), user.getUsername());
        jwtRegistry.registerJwtInformation(new JwtInformation(userMapper.toDto(user, true),
                pastProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole()), oldToken));
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        ResponseEntity<String> response = refresh(oldToken);

        Instant after = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Claims access = assertTokenClaims(accessToken(response), JwtTokenProvider.TokenType.ACCESS, user);
        Claims refresh = assertTokenClaims(cookie(response, "REFRESH_TOKEN").getValue(),
                JwtTokenProvider.TokenType.REFRESH, user);
        assertThat(cookie(response, "REFRESH_TOKEN").getValue()).isNotEqualTo(oldToken);
        assertThat(access.getIssuedAt().toInstant()).isBetween(before, after).isAfter(past);
        assertThat(refresh.getIssuedAt().toInstant()).isBetween(before, after).isAfter(past);
        assertThat(access.getExpiration().toInstant())
                .isEqualTo(access.getIssuedAt().toInstant().plus(jwtProperties.accessTokenValidity()));
        assertThat(refresh.getExpiration().toInstant())
                .isEqualTo(refresh.getIssuedAt().toInstant().plus(jwtProperties.refreshTokenValidity()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired", "tampered", "access", "malformed", "empty"})
    @DisplayName("만료·변조·ACCESS·잘못된 형식·빈 토큰은 갱신 실패 401로 거부한다")
    void refresh_rejectsInvalidRefreshCookie(String scenario) throws Exception {
        User user = createUser();
        String token = switch (scenario) {
            case "expired" -> new JwtTokenProvider(jwtProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
                    .generateRefreshToken(user.getId(), user.getUsername());
            case "tampered" -> {
                String[] parts = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername()).split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                parts[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(
                        payload.replace(user.getId().toString(), UUID.randomUUID().toString())
                                .getBytes(StandardCharsets.UTF_8));
                yield String.join(".", parts);
            }
            case "access" -> jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
            case "malformed" -> "not-a-jwt";
            case "empty" -> "";
            default -> throw new IllegalArgumentException("지원하지 않는 테스트 시나리오: " + scenario);
        };

        // 레지스트리 존재 검사 이후 JWT 검증·타입 검사까지 도달하는 실패 경로를 검증한다.
        jwtRegistry.registerJwtInformation(new JwtInformation(userMapper.toDto(user, true), "unused-access", token));

        assertRenewalFailure(refresh(token));
    }

    @Test
    @DisplayName("유효한 CSRF 정보가 있어도 리프레시 쿠키가 없으면 갱신 실패 JSON과 401을 반환한다")
    void refresh_returnsRenewalFailureWhenCookieIsMissing() throws Exception {
        assertRenewalFailure(refresh(null));
    }

    @Test
    @DisplayName("유효한 리프레시 토큰의 사용자가 삭제되면 500 대신 갱신 실패 401을 반환한다")
    void refresh_returnsRenewalFailureAfterUserDeleted() throws Exception {
        User user = createUser();
        String refreshToken = cookie(login(user), "REFRESH_TOKEN").getValue();
        userRepository.deleteById(user.getId());
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isPresent();

        assertRenewalFailure(refresh(refreshToken));
    }

    @Test
    @DisplayName("정상 리프레시 쿠키가 있어도 CSRF 정보가 없으면 403으로 거부한다")
    void refresh_rejectsRequestWithoutCsrf() throws Exception {
        String refreshToken = cookie(login(createUser()), "REFRESH_TOKEN").getValue();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.COOKIE, "REFRESH_TOKEN=" + refreshToken);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh", new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertJsonContentType(response);
        assertThat(objectMapper.readTree(response.getBody()).path("code").asText()).isEqualTo("AUTH_403");
        assertNoRefreshCookie(response);
    }

    @Test
    @DisplayName("ACCESS와 세션 없이 로그아웃하여 로그인에서 받은 리프레시 쿠키를 삭제한다")
    void logout_clearsLoginRefreshCookieWithoutAccessTokenOrSession() throws Exception {
        User user = createUser();
        ResponseEntity<String> loginResponse = login(user);
        ResponseEntity<String> otherLogin = login(createUser());
        assertUserOnline(getUsers(accessToken(otherLogin)), user, true);
        HttpCookie issuedCookie = cookie(loginResponse, "REFRESH_TOKEN");

        ResponseEntity<String> response = logout(issuedCookie.getValue(), true);

        HttpCookie deletedCookie = assertLogoutResponse(response);
        assertThat(deletedCookie.getPath()).isEqualTo(issuedCookie.getPath());
        assertThat(deletedCookie.getDomain()).isEqualTo(issuedCookie.getDomain());
        assertNoSessionCookie(loginResponse);
        // 로그아웃 시 CSRF 쿠키도 정리되므로 refresh 헬퍼에서 새 CSRF 쿠키·헤더를 준비한다.
        assertUnauthorized(getUsers(accessToken(loginResponse)));
        assertRenewalFailure(refresh(issuedCookie.getValue()));
        assertThat(jwtRegistry.hasActiveJwtInformationByUserId(user.getId())).isFalse();
        assertUserOnline(getUsers(accessToken(otherLogin)), user, false);
        assertThat(refresh(cookie(otherLogin, "REFRESH_TOKEN").getValue()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertRenewalFailure(refresh(null));
        assertUnauthorized(getUsers(null));
    }

    @Test
    @DisplayName("리프레시 쿠키와 인증 정보 없이 로그아웃을 반복해도 삭제 쿠키와 204를 반환한다")
    void logout_returnsNoContentWhenRepeatedWithoutRefreshCookie() {
        ResponseEntity<String> firstResponse = logout(null, true);
        ResponseEntity<String> secondResponse = logout(null, true);

        assertLogoutResponse(firstResponse);
        assertLogoutResponse(secondResponse);
    }

    @Test
    @DisplayName("정상 리프레시 쿠키가 있어도 CSRF 정보 없는 로그아웃은 쿠키 삭제 없이 403으로 거부한다")
    void logout_rejectsRequestWithoutCsrf() throws Exception {
        String refreshToken = cookie(login(createUser()), "REFRESH_TOKEN").getValue();

        ResponseEntity<String> response = logout(refreshToken, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertJsonContentType(response);
        assertThat(objectMapper.readTree(response.getBody()).path("code").asText()).isEqualTo("AUTH_403");
        assertNoRefreshCookie(response);
        assertNoSessionCookie(response);
    }

    @Test
    @DisplayName("JWT 필터는 서블릿에 직접 등록되지 않고 Security 체인에서 폼 로그인 앞에 한 번만 등록된다")
    void jwtFilter_isOnlyRegisteredInSecurityChain() {
        Map<String, ? extends FilterRegistration> registrations = servletContext.getFilterRegistrations();

        assertThat(registrations)
                .doesNotContainKeys("jwtAuthenticationFilter", "discodeitAuthenticationFilterRegistration");
        assertThat(registrations.values())
                .noneMatch(registration -> JwtAuthenticationFilter.class.getName().equals(registration.getClassName()));

        List<Filter> filters = securityFilterChain.getFilters();
        assertThat(filters.stream().filter(filter -> filter == jwtAuthenticationFilter).count()).isEqualTo(1L);
        Filter formLoginFilter = filters.stream()
                .filter(UsernamePasswordAuthenticationFilter.class::isInstance)
                .findFirst()
                .orElseThrow(() -> new AssertionError("폼 로그인 필터가 Security 체인에 없습니다."));
        assertThat(filters.indexOf(jwtAuthenticationFilter)).isLessThan(filters.indexOf(formLoginFilter));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "springSecurityFilterChain",
            "characterEncodingFilter",
            "formContentFilter",
            "requestContextFilter",
            "webMvcObservationFilter"
    })
    @DisplayName("JWT 필터의 자동 등록을 제외해도 기존 서블릿 필터와 전체 경로 매핑은 유지한다")
    void existingServletFilters_remainRegistered(String filterName) {
        FilterRegistration registration = servletContext.getFilterRegistration(filterName);

        assertThat(registration).as("기존 서블릿 필터 %s", filterName).isNotNull();
        assertThat(registration.getUrlPatternMappings()).contains("/*");
    }

    @Test
    @DisplayName("재로그인은 기존 ACCESS·REFRESH를 폐기하고 새 로그인만 유지한다")
    void login_invalidatesPreviousTokenPair() throws Exception {
        User user = createUser();
        ResponseEntity<String> first = login(user);
        ResponseEntity<String> second = login(user);
        String oldRefresh = cookie(first, "REFRESH_TOKEN").getValue();
        String newRefresh = cookie(second, "REFRESH_TOKEN").getValue();

        assertThat(accessToken(second)).isNotEqualTo(accessToken(first));
        assertThat(newRefresh).isNotEqualTo(oldRefresh);
        assertUnauthorized(getUsers(accessToken(first)));
        assertRenewalFailure(refresh(oldRefresh));
        assertUserOnline(getUsers(accessToken(second)), user, true);
        assertThat(refresh(newRefresh).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("갱신 성공 후 이전 토큰 쌍은 재사용할 수 없고 새 토큰 쌍만 사용할 수 있다")
    void refresh_rotatesBothTokensAndRejectsReplay() throws Exception {
        User user = createUser();
        ResponseEntity<String> initial = login(user);
        String oldRefresh = cookie(initial, "REFRESH_TOKEN").getValue();

        ResponseEntity<String> rotated = refresh(oldRefresh);

        assertThat(rotated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accessToken(rotated)).isNotEqualTo(accessToken(initial));
        String newRefresh = cookie(rotated, "REFRESH_TOKEN").getValue();
        assertThat(newRefresh).isNotEqualTo(oldRefresh);
        assertUnauthorized(getUsers(accessToken(initial)));
        assertRenewalFailure(refresh(oldRefresh));
        assertUserOnline(getUsers(accessToken(rotated)), user, true);
        assertThat(refresh(newRefresh).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("동일 리프레시 쿠키의 동시 HTTP 요청은 200 하나와 새 쿠키 없는 401 하나를 반환한다")
    void refresh_concurrentRequestsReturnOneSuccessAndOneUnauthorized() throws Exception {
        ResponseEntity<String> login = login(createUser());
        String oldRefresh = cookie(login, "REFRESH_TOKEN").getValue();
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ResponseEntity<String>> first = executor.submit(() -> {
                start.await(5, SECONDS);
                return refresh(oldRefresh);
            });
            Future<ResponseEntity<String>> second = executor.submit(() -> {
                start.await(5, SECONDS);
                return refresh(oldRefresh);
            });
            List<ResponseEntity<String>> responses = List.of(first.get(10, SECONDS), second.get(10, SECONDS));
            assertThat(responses).extracting(ResponseEntity::getStatusCode)
                    .containsExactlyInAnyOrder(HttpStatus.OK, HttpStatus.UNAUTHORIZED);
            ResponseEntity<String> success = responses.stream()
                    .filter(response -> response.getStatusCode().equals(HttpStatus.OK)).findFirst().orElseThrow();
            ResponseEntity<String> failure = responses.stream()
                    .filter(response -> response.getStatusCode().equals(HttpStatus.UNAUTHORIZED)).findFirst().orElseThrow();

            assertRenewalFailure(failure);
            assertRefreshCookieAttributes(success);
            assertUnauthorized(getUsers(accessToken(login)));
            assertThat(getUsers(accessToken(success)).getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(jwtRegistry.hasActiveJwtInformationByRefreshToken(
                    cookie(success, "REFRESH_TOKEN").getValue())).isTrue();
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }

    @Test
    @DisplayName("서명과 만료가 유효해도 등록되지 않은 토큰은 인증과 갱신에 사용할 수 없다")
    void unregisteredTokens_areRejected() throws Exception {
        User user = createUser();
        String access = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());
        assertThat(jwtTokenProvider.validateToken(access)).isPresent();
        assertThat(jwtTokenProvider.validateToken(refresh)).isPresent();

        assertUnauthorized(getUsers(access));
        assertRenewalFailure(refresh(refresh));
        assertThat(jwtRegistry.hasActiveJwtInformationByUserId(user.getId())).isFalse();
    }

    @Test
    @DisplayName("권한 변경 API는 대상 사용자만 강제 로그아웃하고 다른 사용자의 인증은 유지한다")
    void updateRole_invalidatesOnlyTargetUser() throws Exception {
        User admin = createUser();
        admin.updateRole(new UserRoleUpdateCommand(Role.ADMIN));
        userRepository.saveAndFlush(admin);
        ResponseEntity<String> adminLogin = login(admin);
        User target = createUser();
        ResponseEntity<String> targetLogin = login(target);
        User other = createUser();
        ResponseEntity<String> otherLogin = login(other);
        assertUserOnline(getUsers(accessToken(otherLogin)), target, true);

        ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token", String.class);
        HttpCookie csrfCookie = cookie(csrfResponse, "XSRF-TOKEN");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken(adminLogin));
        headers.set(HttpHeaders.COOKIE, csrfCookie.getName() + "=" + csrfCookie.getValue());
        headers.set("X-XSRF-TOKEN", csrfCookie.getValue());

        ResponseEntity<String> updated = restTemplate.exchange("/api/auth/role", HttpMethod.PUT,
                new HttpEntity<>(new UserRoleUpdateRequest(target.getId(), Role.CHANNEL_MANAGER), headers), String.class);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(updated.getBody());
        assertThat(body.path("role").asText()).isEqualTo(Role.CHANNEL_MANAGER.name());
        assertThat(body.path("online").asBoolean()).isFalse();
        assertThat(userRepository.findById(target.getId()).orElseThrow().getRole()).isEqualTo(Role.CHANNEL_MANAGER);
        assertUnauthorized(getUsers(accessToken(targetLogin)));
        assertRenewalFailure(refresh(cookie(targetLogin, "REFRESH_TOKEN").getValue()));
        assertUserOnline(getUsers(accessToken(otherLogin)), target, false);
        assertUserOnline(getUsers(accessToken(adminLogin)), other, true);
        assertThat(refresh(cookie(otherLogin, "REFRESH_TOKEN").getValue()).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> relogin = login(target);
        assertThat(objectMapper.readTree(relogin.getBody()).path("userDto").path("role").asText())
                .isEqualTo(Role.CHANNEL_MANAGER.name());
        assertUserOnline(getUsers(accessToken(relogin)), target, true);
    }

    @Test
    @DisplayName("만료 정리는 리프레시 만료 사용자만 오프라인으로 바꾸고 유효한 로그인은 유지한다")
    void cleanup_removesExpiredRefreshAndUpdatesOnlineState() throws Exception {
        User expiredUser = createUser();
        ResponseEntity<String> validLogin = login(createUser());
        JwtTokenProvider pastProvider = new JwtTokenProvider(jwtProperties, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        String expiredRefresh = pastProvider.generateRefreshToken(expiredUser.getId(), expiredUser.getUsername());
        jwtRegistry.registerJwtInformation(new JwtInformation(userMapper.toDto(expiredUser, true),
                pastProvider.generateAccessToken(expiredUser.getId(), expiredUser.getUsername(), expiredUser.getRole()),
                expiredRefresh));
        assertUserOnline(getUsers(accessToken(validLogin)), expiredUser, true);

        jwtRegistry.clearExpiredJwtInformation();

        assertThat(jwtRegistry.hasActiveJwtInformationByUserId(expiredUser.getId())).isFalse();
        assertUserOnline(getUsers(accessToken(validLogin)), expiredUser, false);
        assertRenewalFailure(refresh(expiredRefresh));
        assertThat(refresh(cookie(validLogin, "REFRESH_TOKEN").getValue()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void assertUserOnline(ResponseEntity<String> response, User user, boolean online) throws Exception {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(response.getBody())).anySatisfy(item -> {
            assertThat(item.path("id").asText()).isEqualTo(user.getId().toString());
            assertThat(item.path("online").asBoolean()).isEqualTo(online);
        });
    }

    private User createUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User(new UserCreateCommand(
                "jwt-user-" + suffix,
                passwordEncoder.encode(RAW_PASSWORD),
                "jwt-" + suffix + "@example.com"
        ), null);
        // 테스트에 @Transactional을 붙이지 않아 서버 스레드에서도 커밋된 사용자를 조회할 수 있다.
        User savedUser = userRepository.saveAndFlush(user);
        createdUserIds.add(savedUser.getId());
        return savedUser;
    }

    private ResponseEntity<String> login(User user) {
        ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token", String.class);
        assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        HttpCookie csrfCookie = cookie(csrfResponse, "XSRF-TOKEN");

        // TestRestTemplate의 자동 쿠키 저장은 활성화하지 않고 로그인에 필요한 CSRF 쿠키만 전달한다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.COOKIE, csrfCookie.getName() + "=" + csrfCookie.getValue());
        headers.set("X-XSRF-TOKEN", csrfCookie.getValue());
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", user.getUsername());
        form.add("password", RAW_PASSWORD);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", new HttpEntity<>(form, headers), String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonContentType(response);
        return response;
    }

    private String accessToken(ResponseEntity<String> loginResponse) throws Exception {
        String token = objectMapper.readTree(loginResponse.getBody()).path("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private ResponseEntity<String> refresh(String refreshToken) {
        return refresh(refreshToken, null);
    }

    private ResponseEntity<String> refresh(String refreshToken, String accessToken) {
        // 로그인에서 사용한 CSRF 쿠키를 재사용하지 않고 갱신 요청용 쿠키·헤더를 명시적으로 준비한다.
        ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token", String.class);
        assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        HttpCookie csrfCookie = cookie(csrfResponse, "XSRF-TOKEN");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        String cookieHeader = csrfCookie.getName() + "=" + csrfCookie.getValue();
        if (refreshToken != null) {
            cookieHeader += "; REFRESH_TOKEN=" + refreshToken;
        }
        headers.set(HttpHeaders.COOKIE, cookieHeader);
        headers.set("X-XSRF-TOKEN", csrfCookie.getValue());
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return restTemplate.postForEntity("/api/auth/refresh", new HttpEntity<>(headers), String.class);
    }

    private ResponseEntity<String> logout(String refreshToken, boolean withCsrf) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        List<String> requestCookies = new ArrayList<>();
        if (withCsrf) {
            ResponseEntity<String> csrfResponse = restTemplate.getForEntity("/api/auth/csrf-token", String.class);
            assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            HttpCookie csrfCookie = cookie(csrfResponse, "XSRF-TOKEN");
            requestCookies.add(csrfCookie.getName() + "=" + csrfCookie.getValue());
            headers.set("X-XSRF-TOKEN", csrfCookie.getValue());
        }
        if (refreshToken != null) {
            requestCookies.add("REFRESH_TOKEN=" + refreshToken);
        }
        if (!requestCookies.isEmpty()) {
            headers.set(HttpHeaders.COOKIE, String.join("; ", requestCookies));
        }
        return restTemplate.postForEntity("/api/auth/logout", new HttpEntity<>(headers), String.class);
    }

    private HttpCookie assertLogoutResponse(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNullOrEmpty();
        assertThat(response.getHeaders()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
        List<HttpCookie> refreshCookies = cookies(response).stream()
                .filter(cookie -> "REFRESH_TOKEN".equals(cookie.getName()))
                .toList();
        assertThat(refreshCookies).hasSize(1);
        HttpCookie deletedCookie = refreshCookies.get(0);
        assertThat(deletedCookie.getValue()).isEmpty();
        assertThat(deletedCookie.getMaxAge()).isZero();
        assertThat(deletedCookie.isHttpOnly()).isEqualTo(refreshCookieProperties.httpOnly());
        assertThat(deletedCookie.getSecure()).isEqualTo(refreshCookieProperties.secure());
        assertThat(deletedCookie.getPath()).isNull();
        assertThat(deletedCookie.getDomain()).isNull();
        assertThat(response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE))
                .filteredOn(header -> header.startsWith("REFRESH_TOKEN="))
                .singleElement()
                .satisfies(header -> assertThat(header)
                        .contains("SameSite=" + refreshCookieProperties.sameSite()));
        assertNoSessionCookie(response);
        return deletedCookie;
    }

    private Claims assertTokenClaims(String token, JwtTokenProvider.TokenType type, User user) {
        Claims claims = jwtTokenProvider.validateToken(token).orElseThrow();
        assertThat(jwtTokenProvider.getTokenType(claims)).isEqualTo(type.name());
        assertThat(jwtTokenProvider.getUserId(claims)).isEqualTo(user.getId());
        assertThat(claims.getSubject()).isEqualTo(user.getUsername());
        assertThat(claims.getIssuer()).isEqualTo(jwtProperties.issuer());
        if (type == JwtTokenProvider.TokenType.ACCESS) {
            assertThat(claims.get("role", String.class)).isEqualTo(user.getRole().name());
        } else {
            assertThat(claims).doesNotContainKey("role");
        }
        return claims;
    }

    private void assertRefreshCookieAttributes(ResponseEntity<String> response) {
        // 삭제 쿠키와 새 쿠키를 중복 전송하지 않고, 정상 형식의 새 쿠키 하나만 발급한다.
        List<HttpCookie> refreshCookies = cookies(response).stream()
                .filter(cookie -> "REFRESH_TOKEN".equals(cookie.getName()))
                .toList();
        assertThat(refreshCookies).hasSize(1);
        HttpCookie refreshCookie = refreshCookies.get(0);
        assertThat(refreshCookie.getValue()).isNotBlank();
        assertThat(refreshCookie.isHttpOnly()).isEqualTo(refreshCookieProperties.httpOnly());
        assertThat(refreshCookie.getSecure()).isEqualTo(refreshCookieProperties.secure());
        assertThat(refreshCookie.getMaxAge()).isPositive()
                .isEqualTo(jwtProperties.refreshTokenValidity().getSeconds());
        assertThat(response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE))
                .filteredOn(header -> header.startsWith("REFRESH_TOKEN="))
                .singleElement()
                .satisfies(header -> assertThat(header)
                        .contains("SameSite=" + refreshCookieProperties.sameSite()));
    }

    private void assertRenewalFailure(ResponseEntity<String> response) throws Exception {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertJsonContentType(response);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("status").asInt()).isEqualTo(401);
        assertThat(body.path("code").asText()).isEqualTo("TOKEN_RENEWAL_FAILED");
        assertThat(body.findValues("accessToken")).isEmpty();
        assertThat(body.findValues("refreshToken")).isEmpty();
        assertNoRefreshCookie(response);
    }

    private void assertNoRefreshCookie(ResponseEntity<String> response) {
        assertThat(cookies(response)).extracting(HttpCookie::getName).doesNotContain("REFRESH_TOKEN");
    }

    private ResponseEntity<String> getUsers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return restTemplate.exchange("/api/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private void assertUnauthorized(ResponseEntity<String> response) throws Exception {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertJsonContentType(response);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("status").asInt()).isEqualTo(401);
        assertThat(body.path("code").asText()).isEqualTo("AUTH_401");
    }

    private void assertJsonContentType(ResponseEntity<String> response) {
        MediaType contentType = response.getHeaders().getContentType();
        assertThat(contentType).isNotNull();
        assertThat(contentType.isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
    }

    private void assertNoSessionCookie(ResponseEntity<String> response) {
        assertThat(cookies(response)).extracting(HttpCookie::getName).doesNotContain("JSESSIONID");
    }

    private HttpCookie cookie(ResponseEntity<String> response, String name) {
        return cookies(response).stream()
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("응답에 " + name + " 쿠키가 없습니다."));
    }

    private List<HttpCookie> cookies(ResponseEntity<String> response) {
        return response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .flatMap(header -> HttpCookie.parse(header).stream())
                .toList();
    }
}
