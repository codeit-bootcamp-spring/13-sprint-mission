package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Session Security 통합 테스트")
class SessionIntegrationTest {

    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private static final String REMEMBER_ME_COOKIE_NAME = "remember-me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionRegistry sessionRegistry;

    @MockitoBean
    private AdminInitializer adminInitializer;

    @Nested
    @DisplayName("동시 세션 제한")
    class SessionConcurrency {

        @Test
        @DisplayName("동일 사용자가 다시 로그인하면 기존 세션을 만료")
        void secondLogin_expiresPreviousSession() throws Exception {
            // given
            User user = saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MockHttpSession firstSession =
                    loginAndGetSession(
                            "user1",
                            "password1"
                    );

            assertThat(firstSession)
                    .isNotNull();

            // when
            MockHttpSession secondSession =
                    loginAndGetSession(
                            "user1",
                            "password1"
                    );

            // then
            assertThat(secondSession)
                    .isNotNull();

            assertThat(secondSession.getId())
                    .isNotEqualTo(firstSession.getId());

            Object principal =
                    findPrincipal(user);

            List<SessionInformation> sessions =
                    sessionRegistry.getAllSessions(
                            principal,
                            true
                    );

            assertThat(sessions)
                    .hasSize(2);

            assertThat(sessions)
                    .filteredOn(SessionInformation::isExpired)
                    .hasSize(1);

            assertThat(sessions)
                    .filteredOn(session -> !session.isExpired())
                    .hasSize(1);

            // 새 세션은 정상적으로 인증 상태 유지
            mockMvc.perform(
                            get("/api/auth/me")
                                    .session(secondSession)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username")
                            .value("user1"));
        }
    }

    @Nested
    @DisplayName("Remember-Me")
    class RememberMe {

        @Test
        @DisplayName("Remember-Me 로그인 시 remember-me 쿠키를 발급")
        void rememberMe_cookieCreated() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Cookie csrfCookie =
                    getCsrfCookie(null);

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
                                    .param("remember-me", "true")
                    )
                    .andExpect(status().isOk())
                    .andReturn();

            // then
            Cookie rememberMeCookie =
                    result.getResponse()
                            .getCookie(REMEMBER_ME_COOKIE_NAME);

            assertThat(rememberMeCookie)
                    .isNotNull();

            assertThat(rememberMeCookie.getValue())
                    .isNotBlank();
        }

        @Test
        @DisplayName("세션 쿠키가 없어도 Remember-Me 쿠키로 자동 로그인")
        void rememberMe_autoLogin() throws Exception {
            // given
            saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MvcResult loginResult =
                    loginWithRememberMe(
                            "user1",
                            "password1"
                    );

            MockHttpSession originalSession =
                    (MockHttpSession) loginResult
                            .getRequest()
                            .getSession(false);

            Cookie rememberMeCookie =
                    loginResult.getResponse()
                            .getCookie(REMEMBER_ME_COOKIE_NAME);

            assertThat(originalSession)
                    .isNotNull();

            assertThat(rememberMeCookie)
                    .isNotNull();

            /*
             * 브라우저에서 JSESSIONID만 삭제한 상황을 재현:
             *
             * 다음 요청에 기존 session은 전달하지 않고
             * remember-me 쿠키만 전달한다.
             */

            // when
            MvcResult result = mockMvc.perform(
                            get("/api/auth/me")
                                    .cookie(rememberMeCookie)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username")
                            .value("user1"))
                    .andExpect(jsonPath("$.email")
                            .value("user1@test.com"))
                    .andExpect(jsonPath("$.role")
                            .value("USER"))
                    .andReturn();

            // then
            MockHttpSession newSession =
                    (MockHttpSession) result
                            .getRequest()
                            .getSession(false);

            assertThat(newSession)
                    .isNotNull();

            assertThat(newSession.getId())
                    .isNotEqualTo(originalSession.getId());
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

    private MvcResult loginWithRememberMe(
            String username,
            String password
    ) throws Exception {

        Cookie csrfCookie =
                getCsrfCookie(null);

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
                                .param("remember-me", "true")
                )
                .andExpect(status().isOk())
                .andReturn();
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

    private Object findPrincipal(User user) {
        return sessionRegistry.getAllPrincipals()
                .stream()
                .filter(principal ->
                        principal instanceof DiscodeitUserDetails details
                                && details.getUserDto()
                                .id()
                                .equals(user.getId())
                )
                .findFirst()
                .orElseThrow();
    }
}
