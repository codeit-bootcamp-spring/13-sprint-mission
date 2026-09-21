package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
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
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 및 Spring Security 통합 테스트")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Nested
    @DisplayName("CSRF 토큰")
    class CsrfTokenTest {

        @Test
        @DisplayName("CSRF 토큰 요청 시 203과 XSRF-TOKEN 쿠키를 반환한다")
        void should_ReturnCsrfTokenCookie_when_CsrfTokenIsRequested()
                throws Exception {

            MvcResult result =
                    mockMvc.perform(
                                    get("/api/auth/csrf-token")
                            )
                            .andExpect(
                                    status()
                                            .isNonAuthoritativeInformation()
                            )
                            .andReturn();

            Cookie csrfCookie =
                    result.getResponse()
                            .getCookie("XSRF-TOKEN");

            assertThat(csrfCookie)
                    .isNotNull();

            assertThat(csrfCookie.getValue())
                    .isNotBlank();

            assertThat(csrfCookie.isHttpOnly())
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName("올바른 사용자 이름과 비밀번호로 로그인하면 200과 사용자 정보를 반환한다")
        void should_ReturnUserResponse_when_CredentialsAreValid()
                throws Exception {

            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED
                                    )
                                    .param(
                                            "username",
                                            "securityUser"
                                    )
                                    .param(
                                            "password",
                                            "password"
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("securityUser")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value(
                                            "security-user@test.com"
                                    )
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .isNotEmpty()
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value("USER")
                    );
        }

        @Test
        @DisplayName("비밀번호가 틀리면 401과 ErrorResponse를 반환한다")
        void should_ReturnUnauthorized_when_PasswordIsIncorrect()
                throws Exception {

            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED
                                    )
                                    .param(
                                            "username",
                                            "securityUser"
                                    )
                                    .param(
                                            "password",
                                            "wrongPassword"
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    )
                    .andExpect(
                            jsonPath("$.status")
                                    .value(401)
                    )
                    .andExpect(
                            jsonPath("$.code")
                                    .value("AUTH-001")
                    )
                    .andExpect(
                            jsonPath("$.message")
                                    .value(
                                            "아이디 또는 비밀번호가 올바르지 않습니다."
                                    )
                    )
                    .andExpect(
                            jsonPath("$.exceptionType")
                                    .value(
                                            "BadCredentialsException"
                                    )
                    );
        }

        @Test
        @DisplayName("CSRF 토큰 없이 로그인하면 403 Forbidden을 반환한다")
        void should_ReturnForbidden_when_CsrfTokenIsMissing()
                throws Exception {

            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            mockMvc.perform(
                            post("/api/auth/login")
                                    .contentType(
                                            MediaType.APPLICATION_FORM_URLENCODED
                                    )
                                    .param(
                                            "username",
                                            "securityUser"
                                    )
                                    .param(
                                            "password",
                                            "password"
                                    )
                    )
                    .andExpect(
                            status().isForbidden()
                    );
        }

        @Test
        @DisplayName("동일한 계정으로 다시 로그인하면 기존 세션이 만료되고 새 세션이 생성된다")
        void should_ExpirePreviousSession_when_SameUserLogsInAgain()
                throws Exception {

            // given
            createUser(
                    "concurrentUser",
                    "concurrent-user@test.com",
                    "password"
            );

            CsrfData firstCsrfData =
                    getCsrfData();

            // 첫 번째 로그인
            MvcResult firstLoginResult =
                    mockMvc.perform(
                                    post("/api/auth/login")
                                            .cookie(
                                                    firstCsrfData.cookie()
                                            )
                                            .header(
                                                    "X-XSRF-TOKEN",
                                                    firstCsrfData.token()
                                            )
                                            .contentType(
                                                    MediaType.APPLICATION_FORM_URLENCODED
                                            )
                                            .param(
                                                    "username",
                                                    "concurrentUser"
                                            )
                                            .param(
                                                    "password",
                                                    "password"
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andReturn();

            MockHttpSession firstSession =
                    (MockHttpSession) firstLoginResult
                            .getRequest()
                            .getSession(false);

            assertThat(firstSession)
                    .isNotNull();

            SessionInformation firstSessionBeforeSecondLogin =
                    sessionRegistry.getSessionInformation(
                            firstSession.getId()
                    );

            assertThat(firstSessionBeforeSecondLogin)
                    .isNotNull();

            assertThat(
                    firstSessionBeforeSecondLogin.isExpired()
            )
                    .isFalse();

            CsrfData secondCsrfData =
                    getCsrfData();

            // when - 같은 계정으로 두 번째 로그인
            MvcResult secondLoginResult =
                    mockMvc.perform(
                                    post("/api/auth/login")
                                            .cookie(
                                                    secondCsrfData.cookie()
                                            )
                                            .header(
                                                    "X-XSRF-TOKEN",
                                                    secondCsrfData.token()
                                            )
                                            .contentType(
                                                    MediaType.APPLICATION_FORM_URLENCODED
                                            )
                                            .param(
                                                    "username",
                                                    "concurrentUser"
                                            )
                                            .param(
                                                    "password",
                                                    "password"
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andReturn();

            // then - 첫 번째 세션은 만료되어야 한다.
            SessionInformation firstSessionAfterSecondLogin =
                    sessionRegistry.getSessionInformation(
                            firstSession.getId()
                    );

            assertThat(firstSessionAfterSecondLogin)
                    .isNotNull();

            assertThat(
                    firstSessionAfterSecondLogin.isExpired()
            )
                    .isTrue();

            // 두 번째 로그인으로 새 세션은 정상 생성되어야 한다.
            MockHttpSession secondSession =
                    (MockHttpSession) secondLoginResult
                            .getRequest()
                            .getSession(false);

            assertThat(secondSession)
                    .isNotNull();

            assertThat(secondSession.getId())
                    .isNotEqualTo(
                            firstSession.getId()
                    );

            SessionInformation secondSessionInformation =
                    sessionRegistry.getSessionInformation(
                            secondSession.getId()
                    );

            assertThat(secondSessionInformation)
                    .isNotNull();

            assertThat(
                    secondSessionInformation.isExpired()
            )
                    .isFalse();
        }

        @Test
        @DisplayName("remember-me가 true이면 세션 없이도 Remember-Me 쿠키로 인증 상태가 유지된다")
        void should_AuthenticateWithRememberMe_when_SessionIsMissing()
                throws Exception {

            // given
            createUser(
                    "rememberUser",
                    "remember-user@test.com",
                    "password"
            );

            CsrfData csrfData =
                    getCsrfData();

            /*
             * 로그인 유지가 체크된 상태.
             *
             * remember-me=true를 전달한다.
             */
            MvcResult loginResult =
                    mockMvc.perform(
                                    post("/api/auth/login")
                                            .cookie(
                                                    csrfData.cookie()
                                            )
                                            .header(
                                                    "X-XSRF-TOKEN",
                                                    csrfData.token()
                                            )
                                            .contentType(
                                                    MediaType.APPLICATION_FORM_URLENCODED
                                            )
                                            .param(
                                                    "username",
                                                    "rememberUser"
                                            )
                                            .param(
                                                    "password",
                                                    "password"
                                            )
                                            .param(
                                                    "remember-me",
                                                    "true"
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andReturn();

            Cookie rememberMeCookie =
                    loginResult.getResponse()
                            .getCookie("remember-me");

            assertThat(rememberMeCookie)
                    .isNotNull();

            assertThat(rememberMeCookie.getValue())
                    .isNotBlank();

            MockHttpSession originalSession =
                    (MockHttpSession) loginResult
                            .getRequest()
                            .getSession(false);

            assertThat(originalSession)
                    .isNotNull();

            /*
             * 아래 요청에서는 originalSession을 전달하지 않는다.
             *
             * 즉 클라이언트에서 JSESSIONID 쿠키를 삭제한 상황을
             * MockMvc에서 재현한다.
             *
             * Remember-Me 쿠키만 전달한다.
             */
            MvcResult rememberMeResult =
                    mockMvc.perform(
                                    get("/api/auth/me")
                                            .cookie(
                                                    rememberMeCookie
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andExpect(
                                    jsonPath("$.username")
                                            .value(
                                                    "rememberUser"
                                            )
                            )
                            .andExpect(
                                    jsonPath("$.email")
                                            .value(
                                                    "remember-user@test.com"
                                            )
                            )
                            .andExpect(
                                    jsonPath("$.role")
                                            .value("USER")
                            )
                            .andReturn();

            /*
             * Remember-Me 인증 후 새로운 세션이 생성되었는지 확인한다.
             */
            MockHttpSession rememberMeSession =
                    (MockHttpSession) rememberMeResult
                            .getRequest()
                            .getSession(false);

            assertThat(rememberMeSession)
                    .isNotNull();

            assertThat(rememberMeSession.getId())
                    .isNotEqualTo(
                            originalSession.getId()
                    );

            /*
             * 새로운 Remember-Me 세션이 활성 상태인지 확인한다.
             */
            SessionInformation rememberMeSessionInformation =
                    sessionRegistry.getSessionInformation(
                            rememberMeSession.getId()
                    );

            assertThat(rememberMeSessionInformation)
                    .isNotNull();

            assertThat(
                    rememberMeSessionInformation.isExpired()
            )
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("현재 사용자 조회")
    class CurrentUserTest {

        @Test
        @DisplayName("로그인 세션으로 현재 사용자 정보를 조회하면 200과 사용자 정보를 반환한다")
        void should_ReturnCurrentUser_when_AuthenticatedSessionExists()
                throws Exception {

            createUser(
                    "currentUser",
                    "current-user@test.com",
                    "password"
            );

            MockHttpSession session =
                    login(
                            "currentUser",
                            "password"
                    );

            mockMvc.perform(
                            get("/api/auth/me")
                                    .session(session)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .isNotEmpty()
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("currentUser")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value(
                                            "current-user@test.com"
                                    )
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value("USER")
                    )
                    .andExpect(
                            jsonPath("$.online")
                                    .value(true)
                    );
        }

        @Test
        @DisplayName("인증되지 않은 상태에서 현재 사용자 조회 시 401을 반환한다")
        void should_ReturnUnauthorized_when_CurrentUserIsNotAuthenticated()
                throws Exception {

            mockMvc.perform(
                            get("/api/auth/me")
                    )
                    .andExpect(
                            status().isUnauthorized()
                    );
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @DisplayName("로그인 사용자가 로그아웃하면 204 No Content를 반환한다")
        void should_ReturnNoContent_when_LogoutSucceeds()
                throws Exception {

            createUser(
                    "logoutUser",
                    "logout-user@test.com",
                    "password"
            );

            MockHttpSession session =
                    login(
                            "logoutUser",
                            "password"
                    );

            CsrfData logoutCsrfData =
                    getCsrfData();

            mockMvc.perform(
                            post("/api/auth/logout")
                                    .session(session)
                                    .cookie(
                                            logoutCsrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            logoutCsrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isNoContent()
                    );
        }

        @Test
        @DisplayName("로그아웃 후 현재 사용자 조회 시 401 Unauthorized를 반환한다")
        void should_ReturnUnauthorized_when_AccessingMeAfterLogout()
                throws Exception {

            createUser(
                    "logoutMeUser",
                    "logout-me-user@test.com",
                    "password"
            );

            MockHttpSession session =
                    login(
                            "logoutMeUser",
                            "password"
                    );

            CsrfData logoutCsrfData =
                    getCsrfData();

            mockMvc.perform(
                            post("/api/auth/logout")
                                    .session(session)
                                    .cookie(
                                            logoutCsrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            logoutCsrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isNoContent()
                    );

            mockMvc.perform(
                            get("/api/auth/me")
                    )
                    .andExpect(
                            status().isUnauthorized()
                    );
        }
    }

    @Nested
    @DisplayName("권한")
    class RoleTest {

        @Test
        @DisplayName("회원가입 시 기본 권한은 USER이다")
        void should_AssignUserRole_when_UserIsCreated()
                throws Exception {

            UserCreateRequest request =
                    new UserCreateRequest(
                            "roleUser",
                            "role-user@test.com",
                            "password",
                            null
                    );

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            post("/api/users")
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    request
                                            )
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value("roleUser")
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value("USER")
                    );
        }

        @Test
        @DisplayName("ADMIN이 사용자 권한을 CHANNEL_MANAGER로 변경하면 200과 변경된 사용자 정보를 반환한다")
        void should_UpdateRoleToChannelManager_when_AdminRequestsRoleUpdate()
                throws Exception {

            String userId =
                    createUser(
                            "channelManagerUser",
                            "channel-manager@test.com",
                            "password"
                    );

            createAdmin(
                    "roleAdmin1",
                    "role-admin1@test.com",
                    "password"
            );

            MockHttpSession adminSession =
                    login(
                            "roleAdmin1",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(userId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .session(adminSession)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(userId)
                    )
                    .andExpect(
                            jsonPath("$.username")
                                    .value(
                                            "channelManagerUser"
                                    )
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value(
                                            "CHANNEL_MANAGER"
                                    )
                    );
        }

        @Test
        @DisplayName("ADMIN이 사용자 권한을 ADMIN으로 변경하면 200과 ADMIN 권한을 반환한다")
        void should_UpdateRoleToAdmin_when_AdminRequestsRoleUpdate()
                throws Exception {

            String userId =
                    createUser(
                            "adminRoleUser",
                            "admin-role-user@test.com",
                            "password"
                    );

            createAdmin(
                    "roleAdmin2",
                    "role-admin2@test.com",
                    "password"
            );

            MockHttpSession adminSession =
                    login(
                            "roleAdmin2",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "ADMIN"
                    }
                    """.formatted(userId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .session(adminSession)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(userId)
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value("ADMIN")
                    );
        }

        @Test
        @DisplayName("CSRF 토큰 없이 사용자 권한을 수정하면 403 Forbidden을 반환한다")
        void should_ReturnForbidden_when_RoleUpdateHasNoCsrfToken()
                throws Exception {

            String userId =
                    createUser(
                            "csrfRoleUser",
                            "csrf-role-user@test.com",
                            "password"
                    );

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(userId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isForbidden()
                    );
        }

        @Test
        @DisplayName("로그인 중인 사용자의 권한이 변경되면 기존 세션이 만료된다")
        void should_ExpireExistingSession_when_UserRoleIsUpdated()
                throws Exception {

            String targetUserId =
                    createUser(
                            "sessionRoleUser",
                            "session-role-user@test.com",
                            "password"
                    );

            MockHttpSession targetUserSession =
                    login(
                            "sessionRoleUser",
                            "password"
                    );

            SessionInformation beforeUpdate =
                    sessionRegistry.getSessionInformation(
                            targetUserSession.getId()
                    );

            assertThat(beforeUpdate)
                    .isNotNull();

            assertThat(beforeUpdate.isExpired())
                    .isFalse();

            createAdmin(
                    "sessionRoleAdmin",
                    "session-role-admin@test.com",
                    "password"
            );

            MockHttpSession adminSession =
                    login(
                            "sessionRoleAdmin",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody = """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(targetUserId);

            mockMvc.perform(
                            put("/api/auth/role")
                                    .session(adminSession)
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(requestBody)
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .value(targetUserId)
                    )
                    .andExpect(
                            jsonPath("$.role")
                                    .value(
                                            "CHANNEL_MANAGER"
                                    )
                    );

            SessionInformation afterUpdate =
                    sessionRegistry.getSessionInformation(
                            targetUserSession.getId()
                    );

            assertThat(afterUpdate)
                    .isNotNull();

            assertThat(afterUpdate.isExpired())
                    .isTrue();
        }
    }

    private String createUser(
            String username,
            String email,
            String password
    ) throws Exception {

        UserCreateRequest request =
                new UserCreateRequest(
                        username,
                        email,
                        password,
                        null
                );

        CsrfData csrfData =
                getCsrfData();

        MvcResult result =
                mockMvc.perform(
                                post("/api/users")
                                        .cookie(
                                                csrfData.cookie()
                                        )
                                        .header(
                                                "X-XSRF-TOKEN",
                                                csrfData.token()
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        request
                                                )
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        JsonNode body =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );

        return body.get("id")
                .asText();
    }

    private void createAdmin(
            String username,
            String email,
            String password
    ) throws Exception {

        String userId =
                createUser(
                        username,
                        email,
                        password
                );

        User user =
                userRepository.findById(
                                java.util.UUID.fromString(
                                        userId
                                )
                        )
                        .orElseThrow();

        user.updateRole(
                Role.ADMIN
        );

        userRepository.saveAndFlush(
                user
        );
    }

    private MockHttpSession login(
            String username,
            String password
    ) throws Exception {

        CsrfData csrfData =
                getCsrfData();

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .cookie(
                                                csrfData.cookie()
                                        )
                                        .header(
                                                "X-XSRF-TOKEN",
                                                csrfData.token()
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_FORM_URLENCODED
                                        )
                                        .param(
                                                "username",
                                                username
                                        )
                                        .param(
                                                "password",
                                                password
                                        )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(false);

        assertThat(session)
                .isNotNull();

        return session;
    }

    private CsrfData getCsrfData()
            throws Exception {

        MvcResult result =
                mockMvc.perform(
                                get("/api/auth/csrf-token")
                        )
                        .andExpect(
                                status()
                                        .isNonAuthoritativeInformation()
                        )
                        .andReturn();

        Cookie csrfCookie =
                result.getResponse()
                        .getCookie("XSRF-TOKEN");

        assertThat(csrfCookie)
                .isNotNull();

        assertThat(csrfCookie.getValue())
                .isNotBlank();

        return new CsrfData(
                csrfCookie,
                csrfCookie.getValue()
        );
    }

    private record CsrfData(
            Cookie cookie,
            String token
    ) {
    }
}