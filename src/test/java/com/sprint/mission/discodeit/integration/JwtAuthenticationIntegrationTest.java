package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.HttpCookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:jwt-authentication-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "discodeit.storage.type=local",
        "discodeit.storage.local.root-path=./build/test-uploads/jwt-authentication",
        "discodeit.security.jwt.secret=jwt-authentication-integration-test-secret-with-at-least-32-bytes",
        "discodeit.security.jwt.access-token-validity=10m",
        "discodeit.security.jwt.refresh-token-validity=14d",
        "discodeit.security.jwt.issuer=jwt-authentication-test"
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
    JwtProperties jwtProperties;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    SecurityFilterChain securityFilterChain;

    @Autowired
    ServletContext servletContext;

    private final List<UUID> createdUserIds = new ArrayList<>();

    @AfterEach
    void deleteCreatedUsers() {
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
        createdUserIds.remove(user.getId());
        assertThat(jwtTokenProvider.validateToken(token)).isPresent();

        assertUnauthorized(getUsers(token));
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
        assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.NON_AUTHORITATIVE_INFORMATION);
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
