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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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
            Cookie csrfCookie =
                    result.getResponse()
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
        @DisplayName("아이디와 비밀번호가 일치하면 로그인에 성공하고 200을 반환")
        void login_success() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Cookie csrfCookie =
                    getCsrfCookie(null);

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
                                    .param("password", "password1")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username")
                            .value("user1"))
                    .andExpect(jsonPath("$.email")
                            .value("user1@test.com"))
                    .andExpect(jsonPath("$.online")
                            .value(true))
                    .andExpect(jsonPath("$.role")
                            .value("USER"));
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

            Cookie csrfCookie =
                    getCsrfCookie(null);

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
            Cookie csrfCookie =
                    getCsrfCookie(null);

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
    }

    @Nested
    @DisplayName("현재 로그인 사용자 조회")
    class CurrentUser {

        @Test
        @DisplayName("로그인한 사용자는 자신의 정보를 조회")
        void me_success() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MockHttpSession session =
                    loginAndGetSession(
                            "user1",
                            "password1"
                    );

            // when & then
            mockMvc.perform(
                            get("/api/auth/me")
                                    .session(session)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username")
                            .value("user1"))
                    .andExpect(jsonPath("$.email")
                            .value("user1@test.com"))
                    .andExpect(jsonPath("$.online")
                            .value(true))
                    .andExpect(jsonPath("$.role")
                            .value("USER"));
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 조회하면 401을 반환")
        void me_fail_unauthenticated() throws Exception {
            // when & then
            mockMvc.perform(
                            get("/api/auth/me")
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code")
                            .value("AUTHENTICATION_REQUIRED"))
                    .andExpect(jsonPath("$.status")
                            .value(401));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("로그인한 사용자가 로그아웃하면 204를 반환")
        void logout_success() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MockHttpSession session =
                    loginAndGetSession(
                            "user1",
                            "password1"
                    );

            // 로그인 이후 사용할 새로운 CSRF 토큰 발급
            Cookie csrfCookie =
                    getCsrfCookie(session);

            // when
            mockMvc.perform(
                            post("/api/auth/logout")
                                    .session(session)
                                    .cookie(csrfCookie)
                                    .header(
                                            CSRF_HEADER_NAME,
                                            csrfCookie.getValue()
                                    )
                    )
                    .andExpect(status().isNoContent());

            // then
            assertThat(session.isInvalid())
                    .isTrue();
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

    private MockHttpSession loginAndGetSession(
            String username,
            String password
    ) throws Exception {

        Cookie csrfCookie =
                getCsrfCookie(null);

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
                                .param("username", username)
                                .param("password", password)
                )
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result
                        .getRequest()
                        .getSession(false);

        assertThat(session)
                .isNotNull();

        return session;
    }

    private Cookie getCsrfCookie(
            MockHttpSession session
    ) throws Exception {

        MockHttpServletRequestBuilder request =
                get("/api/auth/csrf-token");

        if (session != null) {
            request.session(session);
        }

        MvcResult result = mockMvc.perform(request)
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
}