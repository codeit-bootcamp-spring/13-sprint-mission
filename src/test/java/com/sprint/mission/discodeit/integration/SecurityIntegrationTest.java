package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Spring Security 통합 테스트")
class SecurityIntegrationTest {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AdminInitializer adminInitializer;

    @Nested
    @DisplayName("CSRF 토큰 발급")
    class CsrfToken {

        @Test
        @DisplayName("CSRF 토큰을 요청하면 203과 XSRF-TOKEN 쿠키를 반환")
        void getCsrfToken_success() throws Exception {
            // when
            MvcResult result = mockMvc.perform(
                            get("/api/auth/csrf-token")
                    )
                    .andExpect(status().isNonAuthoritativeInformation())
                    .andReturn();

            // then
            Cookie csrfCookie = result.getResponse()
                    .getCookie(CSRF_COOKIE_NAME);

            assertThat(csrfCookie)
                    .isNotNull();

            assertThat(csrfCookie.getValue())
                    .isNotBlank();

            assertThat(csrfCookie.isHttpOnly())
                    .isFalse();

            assertThat(csrfCookie.getPath())
                    .isEqualTo("/");
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("아이디와 비밀번호가 일치하면 JWT를 발급하고 200을 반환")
        void login_success() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Cookie csrfCookie = getCsrfCookie();

            // when
            MvcResult result = mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(csrfCookie)
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED
                                    )
                                    .param("username", "user1")
                                    .param("password", "password1")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andReturn();

            // then
            String responseBody =
                    result.getResponse().getContentAsString();

            assertThat(responseBody)
                    .contains("\"username\":\"user1\"")
                    .contains("\"email\":\"user1@test.com\"")
                    .contains("\"role\":\"USER\"");

            String refreshSetCookie =
                    getRefreshTokenSetCookieHeader(result);

            assertThat(refreshSetCookie)
                    .contains("REFRESH_TOKEN=")
                    .contains("HttpOnly");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 401을 반환")
        void login_fail_wrongPassword() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Cookie csrfCookie = getCsrfCookie();

            // when & then
            mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(csrfCookie)
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED
                                    )
                                    .param("username", "user1")
                                    .param("password", "wrong-password")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code")
                            .value("AUTHENTICATION_FAILED"))
                    .andExpect(jsonPath("$.status")
                            .value(401));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인하면 401을 반환")
        void login_fail_unknownUser() throws Exception {
            // given
            Cookie csrfCookie = getCsrfCookie();

            // when & then
            mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(csrfCookie)
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED
                                    )
                                    .param("username", "unknown")
                                    .param("password", "password1")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code")
                            .value("AUTHENTICATION_FAILED"))
                    .andExpect(jsonPath("$.status")
                            .value(401));
        }

        @Test
        @DisplayName("동일 사용자가 다시 로그인하면 기존 Refresh Token은 무효화")
        void secondLogin_invalidatesPreviousRefreshToken() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MvcResult firstLoginResult =
                    login(
                            "user1",
                            "password1"
                    );

            Cookie firstRefreshToken =
                    getRefreshTokenCookie(firstLoginResult);

            // when
            MvcResult secondLoginResult =
                    login(
                            "user1",
                            "password1"
                    );

            Cookie secondRefreshToken =
                    getRefreshTokenCookie(secondLoginResult);

            assertThat(secondRefreshToken.getValue())
                    .isNotEqualTo(firstRefreshToken.getValue());

            // 첫 번째 로그인에서 발급받은 Refresh Token은 더 이상 사용 불가
            Cookie csrfCookie =
                    getCsrfCookie();

            mockMvc.perform(
                            post("/api/auth/refresh")
                                    .cookie(
                                            firstRefreshToken,
                                            csrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isUnauthorized());

            // 두 번째 로그인에서 발급받은 Refresh Token은 정상 사용 가능
            Cookie newCsrfCookie =
                    getCsrfCookie();

            mockMvc.perform(
                            post("/api/auth/refresh")
                                    .cookie(
                                            secondRefreshToken,
                                            newCsrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            newCsrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Refresh {

        @Test
        @DisplayName("유효한 Refresh Token이면 JWT를 재발급하고 200을 반환")
        void refresh_success() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MvcResult loginResult =
                    login(
                            "user1",
                            "password1"
                    );

            Cookie refreshTokenCookie =
                    getRefreshTokenCookie(loginResult);

            Cookie csrfCookie =
                    getCsrfCookie();

            // when
            MvcResult result = mockMvc.perform(
                            post("/api/auth/refresh")
                                    .cookie(
                                            refreshTokenCookie,
                                            csrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken")
                            .isNotEmpty())
                    .andReturn();

            // then
            String refreshSetCookie =
                    getRefreshTokenSetCookieHeader(result);

            assertThat(refreshSetCookie)
                    .contains("REFRESH_TOKEN=")
                    .contains("HttpOnly");
        }

        @Test
        @DisplayName("토큰 재발급 후 기존 Refresh Token은 재사용할 수 없다")
        void refresh_fail_reusedRefreshToken() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MvcResult loginResult =
                    login(
                            "user1",
                            "password1"
                    );

            Cookie oldRefreshToken =
                    getRefreshTokenCookie(loginResult);

            Cookie csrfCookie =
                    getCsrfCookie();

            // 첫 번째 재발급 성공 → Refresh Token Rotation
            mockMvc.perform(
                            post("/api/auth/refresh")
                                    .cookie(
                                            oldRefreshToken,
                                            csrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isOk());

            // 새로운 CSRF 토큰 발급
            Cookie newCsrfCookie =
                    getCsrfCookie();

            // when & then
            // Rotation으로 무효화된 기존 Refresh Token 재사용
            mockMvc.perform(
                            post("/api/auth/refresh")
                                    .cookie(
                                            oldRefreshToken,
                                            newCsrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            newCsrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status")
                            .value(401));
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token이면 401을 반환")
        void refresh_fail_invalidToken() throws Exception {
            // given
            Cookie csrfCookie =
                    getCsrfCookie();

            Cookie invalidRefreshToken =
                    new Cookie(
                            REFRESH_TOKEN_COOKIE_NAME,
                            "invalid-refresh-token"
                    );

            // when & then
            mockMvc.perform(
                            post("/api/auth/refresh")
                                    .cookie(
                                            invalidRefreshToken,
                                            csrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status")
                            .value(401));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("로그아웃하면 Refresh Token을 무효화하고 쿠키를 삭제한 뒤 204를 반환")
        void logout_success() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MvcResult loginResult =
                    login(
                            "user1",
                            "password1"
                    );

            Cookie refreshTokenCookie =
                    getRefreshTokenCookie(loginResult);

            Cookie csrfCookie =
                    getCsrfCookie();

            // when
            MvcResult result = mockMvc.perform(
                            post("/api/auth/logout")
                                    .cookie(
                                            refreshTokenCookie,
                                            csrfCookie
                                    )
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isNoContent())
                    .andReturn();

            // then
            String deletedRefreshCookie =
                    getRefreshTokenSetCookieHeader(result);

            assertThat(deletedRefreshCookie)
                    .contains("REFRESH_TOKEN=")
                    .contains("Max-Age=0");
        }
    }

    private User saveUser(
            String username,
            String email,
            String rawPassword
    ) {
        User user = new User(
                username,
                email,
                passwordEncoder.encode(rawPassword),
                null,
                Role.USER
        );

        return userRepository.saveAndFlush(user);
    }

    private MvcResult login(
            String username,
            String password
    ) throws Exception {

        Cookie csrfCookie =
                getCsrfCookie();

        return mockMvc.perform(
                        post("/api/auth/login")
                                .cookie(csrfCookie)
                                .header(
                                        CSRF_HEADER_NAME,
                                        csrfCookie.getValue()
                                )
                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )
                                .param("username", username)
                                .param("password", password)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    private Cookie getCsrfCookie() throws Exception {

        MvcResult result = mockMvc.perform(
                        get("/api/auth/csrf-token")
                )
                .andExpect(
                        status()
                                .isNonAuthoritativeInformation()
                )
                .andReturn();

        Cookie csrfCookie =
                result.getResponse()
                        .getCookie(CSRF_COOKIE_NAME);

        assertThat(csrfCookie)
                .isNotNull();

        return csrfCookie;
    }

    private Cookie getRefreshTokenCookie(
            MvcResult result
    ) {
        String setCookie =
                getRefreshTokenSetCookieHeader(result);

        String prefix =
                REFRESH_TOKEN_COOKIE_NAME + "=";

        int start =
                setCookie.indexOf(prefix)
                        + prefix.length();

        int end =
                setCookie.indexOf(";", start);

        String refreshToken =
                end >= 0
                        ? setCookie.substring(start, end)
                        : setCookie.substring(start);

        assertThat(refreshToken)
                .isNotBlank();

        return new Cookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken
        );
    }

    private String getRefreshTokenSetCookieHeader(
            MvcResult result
    ) {
        return result.getResponse()
                .getHeaders(HttpHeaders.SET_COOKIE)
                .stream()
                .filter(header ->
                        header.startsWith(
                                REFRESH_TOKEN_COOKIE_NAME + "="
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "REFRESH_TOKEN Set-Cookie 헤더가 없습니다."
                        )
                );
    }
}