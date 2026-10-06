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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("인증 및 Spring Security 통합 테스트")
class AuthSecurityIntegrationTest {

    private static final String AUTHORIZATION =
            "Authorization";

    private static final String BEARER =
            "Bearer ";

    private static final String REFRESH_TOKEN =
            "REFRESH_TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("CSRF 토큰")
    class CsrfTokenTest {

        @Test
        @DisplayName(
                "CSRF 토큰 요청 시 203과 XSRF-TOKEN 쿠키를 반환한다"
        )
        void should_ReturnCsrfTokenCookie_when_CsrfTokenIsRequested()
                throws Exception {

            MvcResult result =
                    mockMvc.perform(
                                    get(
                                            "/api/auth/csrf-token"
                                    )
                            )
                            .andExpect(
                                    status()
                                            .isNonAuthoritativeInformation()
                            )
                            .andReturn();

            Cookie csrfCookie =
                    result.getResponse()
                            .getCookie(
                                    "XSRF-TOKEN"
                            );

            assertThat(
                    csrfCookie
            ).isNotNull();

            assertThat(
                    csrfCookie.getValue()
            ).isNotBlank();

            assertThat(
                    csrfCookie.isHttpOnly()
            ).isFalse();
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName(
                "올바른 사용자 이름과 비밀번호로 로그인하면 200과 JWT 정보를 반환한다"
        )
        void should_ReturnJwtDto_when_CredentialsAreValid()
                throws Exception {

            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            CsrfData csrfData =
                    getCsrfData();

            MvcResult result =
                    mockMvc.perform(
                                    post(
                                            "/api/auth/login"
                                    )
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
                                    jsonPath(
                                            "$.userDto.username"
                                    )
                                            .value(
                                                    "securityUser"
                                            )
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.userDto.email"
                                    )
                                            .value(
                                                    "security-user@test.com"
                                            )
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.userDto.id"
                                    )
                                            .isNotEmpty()
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.userDto.role"
                                    )
                                            .value(
                                                    "USER"
                                            )
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.accessToken"
                                    )
                                            .isNotEmpty()
                            )
                            .andExpect(
                                    cookie()
                                            .exists(
                                                    REFRESH_TOKEN
                                            )
                            )
                            .andReturn();

            Cookie refreshTokenCookie =
                    result.getResponse()
                            .getCookie(
                                    REFRESH_TOKEN
                            );

            assertThat(
                    refreshTokenCookie
            ).isNotNull();

            assertThat(
                    refreshTokenCookie.getValue()
            ).isNotBlank();

            assertThat(
                    refreshTokenCookie.isHttpOnly()
            ).isTrue();
        }

        @Test
        @DisplayName(
                "비밀번호가 틀리면 401과 ErrorResponse를 반환한다"
        )
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
                            post(
                                    "/api/auth/login"
                            )
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
                            jsonPath(
                                    "$.status"
                            )
                                    .value(
                                            401
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.code"
                            )
                                    .value(
                                            "AUTH-001"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.message"
                            )
                                    .value(
                                            "아이디 또는 비밀번호가 올바르지 않습니다."
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.exceptionType"
                            )
                                    .value(
                                            "BadCredentialsException"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "CSRF 토큰 없이 로그인하면 403 Forbidden을 반환한다"
        )
        void should_ReturnForbidden_when_CsrfTokenIsMissing()
                throws Exception {

            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            mockMvc.perform(
                            post(
                                    "/api/auth/login"
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
                            status().isForbidden()
                    );
        }

        @Test
        @DisplayName(
                "동일한 계정으로 다시 로그인하면 기존 JWT는 무효화되고 새로운 JWT만 활성화된다"
        )
        void should_InvalidatePreviousJwt_when_SameUserLogsInAgain()
                throws Exception {

            String userId =
                    createUser(
                            "concurrentUser",
                            "concurrent-user@test.com",
                            "password"
                    );

            LoginData firstLogin =
                    login(
                            "concurrentUser",
                            "password"
                    );

            LoginData secondLogin =
                    login(
                            "concurrentUser",
                            "password"
                    );

            assertThat(
                    firstLogin.accessToken()
            ).isNotBlank();

            assertThat(
                    secondLogin.accessToken()
            ).isNotBlank();

            assertThat(
                    firstLogin.refreshToken()
            ).isNotBlank();

            assertThat(
                    secondLogin.refreshToken()
            ).isNotBlank();

            assertThat(
                    secondLogin.accessToken()
            ).isNotEqualTo(
                    firstLogin.accessToken()
            );

            assertThat(
                    secondLogin.refreshToken()
            ).isNotEqualTo(
                    firstLogin.refreshToken()
            );

            /*
             * 첫 번째 Access Token은 Registry에서 제거되었으므로
             * 인증되지 않아야 한다.
             */
            mockMvc.perform(
                            get(
                                    "/api/channels"
                            )
                                    .param(
                                            "userId",
                                            userId
                                    )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + firstLogin.accessToken()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    );

            /*
             * 두 번째 로그인에서 발급된 Access Token은
             * 현재 Registry에 등록된 활성 토큰이다.
             */
            mockMvc.perform(
                            get(
                                    "/api/channels"
                            )
                                    .param(
                                            "userId",
                                            userId
                                    )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + secondLogin.accessToken()
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    );
        }
    }

    @Nested
    @DisplayName("Refresh Token 재발급")
    class RefreshTokenTest {

        @Test
        @DisplayName(
                "유효한 Refresh Token으로 재발급하면 200과 새로운 JWT 정보를 반환한다"
        )
        void should_ReturnNewJwtDto_when_RefreshTokenIsValid()
                throws Exception {

            createUser(
                    "refreshUser",
                    "refresh-user@test.com",
                    "password"
            );

            LoginData loginData =
                    login(
                            "refreshUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            MvcResult result =
                    mockMvc.perform(
                                    post(
                                            "/api/auth/refresh"
                                    )
                                            .cookie(
                                                    csrfData.cookie(),
                                                    new Cookie(
                                                            REFRESH_TOKEN,
                                                            loginData.refreshToken()
                                                    )
                                            )
                                            .header(
                                                    "X-XSRF-TOKEN",
                                                    csrfData.token()
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.userDto.username"
                                    )
                                            .value(
                                                    "refreshUser"
                                            )
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.userDto.email"
                                    )
                                            .value(
                                                    "refresh-user@test.com"
                                            )
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.userDto.role"
                                    )
                                            .value(
                                                    "USER"
                                            )
                            )
                            .andExpect(
                                    jsonPath(
                                            "$.accessToken"
                                    )
                                            .isNotEmpty()
                            )
                            .andExpect(
                                    cookie()
                                            .exists(
                                                    REFRESH_TOKEN
                                            )
                            )
                            .andReturn();

            Cookie newRefreshTokenCookie =
                    result.getResponse()
                            .getCookie(
                                    REFRESH_TOKEN
                            );

            assertThat(
                    newRefreshTokenCookie
            ).isNotNull();

            assertThat(
                    newRefreshTokenCookie.getValue()
            ).isNotBlank();

            assertThat(
                    newRefreshTokenCookie.isHttpOnly()
            ).isTrue();
        }

        @Test
        @DisplayName(
                "재발급 후 기존 Refresh Token을 다시 사용하면 401을 반환한다"
        )
        void should_RejectOldRefreshToken_when_RefreshTokenIsRotated()
                throws Exception {

            createUser(
                    "rotationUser",
                    "rotation-user@test.com",
                    "password"
            );

            LoginData loginData =
                    login(
                            "rotationUser",
                            "password"
                    );

            String oldRefreshToken =
                    loginData.refreshToken();

            CsrfData firstCsrfData =
                    getCsrfData();

            MvcResult result =
                    mockMvc.perform(
                                    post(
                                            "/api/auth/refresh"
                                    )
                                            .cookie(
                                                    firstCsrfData.cookie(),
                                                    new Cookie(
                                                            REFRESH_TOKEN,
                                                            oldRefreshToken
                                                    )
                                            )
                                            .header(
                                                    "X-XSRF-TOKEN",
                                                    firstCsrfData.token()
                                            )
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andReturn();

            Cookie newRefreshTokenCookie =
                    result.getResponse()
                            .getCookie(
                                    REFRESH_TOKEN
                            );

            assertThat(
                    newRefreshTokenCookie
            ).isNotNull();

            assertThat(
                    newRefreshTokenCookie.getValue()
            ).isNotBlank();

            assertThat(
                    newRefreshTokenCookie.getValue()
            ).isNotEqualTo(
                    oldRefreshToken
            );

            /*
             * Rotation 이후 Registry에는 새 Refresh Token만 존재한다.
             * 따라서 기존 Refresh Token은 다시 사용할 수 없어야 한다.
             */
            CsrfData secondCsrfData =
                    getCsrfData();

            mockMvc.perform(
                            post(
                                    "/api/auth/refresh"
                            )
                                    .cookie(
                                            secondCsrfData.cookie(),
                                            new Cookie(
                                                    REFRESH_TOKEN,
                                                    oldRefreshToken
                                            )
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            secondCsrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.status"
                            )
                                    .value(
                                            401
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.code"
                            )
                                    .value(
                                            "AUTH-001"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.message"
                            )
                                    .value(
                                            "유효하지 않은 Refresh Token입니다."
                                    )
                    );
        }

        @Test
        @DisplayName(
                "Refresh Token 쿠키가 없으면 401과 ErrorResponse를 반환한다"
        )
        void should_ReturnUnauthorized_when_RefreshTokenIsMissing()
                throws Exception {

            CsrfData csrfData =
                    getCsrfData();

            mockMvc.perform(
                            post(
                                    "/api/auth/refresh"
                            )
                                    .cookie(
                                            csrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.status"
                            )
                                    .value(
                                            401
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.code"
                            )
                                    .value(
                                            "AUTH-001"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.message"
                            )
                                    .value(
                                            "Refresh Token이 존재하지 않습니다."
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.exceptionType"
                            )
                                    .value(
                                            "BadCredentialsException"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "잘못된 Refresh Token이면 401과 ErrorResponse를 반환한다"
        )
        void should_ReturnUnauthorized_when_RefreshTokenIsInvalid()
                throws Exception {

            CsrfData csrfData =
                    getCsrfData();

            Cookie invalidRefreshToken =
                    new Cookie(
                            REFRESH_TOKEN,
                            "invalid.refresh.token"
                    );

            mockMvc.perform(
                            post(
                                    "/api/auth/refresh"
                            )
                                    .cookie(
                                            csrfData.cookie(),
                                            invalidRefreshToken
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.status"
                            )
                                    .value(
                                            401
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.code"
                            )
                                    .value(
                                            "AUTH-001"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.message"
                            )
                                    .value(
                                            "유효하지 않은 Refresh Token입니다."
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.exceptionType"
                            )
                                    .value(
                                            "BadCredentialsException"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "Access Token을 Refresh Token 쿠키로 전달하면 401을 반환한다"
        )
        void should_ReturnUnauthorized_when_AccessTokenIsUsedAsRefreshToken()
                throws Exception {

            createUser(
                    "accessAsRefreshUser",
                    "access-as-refresh@test.com",
                    "password"
            );

            LoginData loginData =
                    login(
                            "accessAsRefreshUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            Cookie wrongRefreshToken =
                    new Cookie(
                            REFRESH_TOKEN,
                            loginData.accessToken()
                    );

            mockMvc.perform(
                            post(
                                    "/api/auth/refresh"
                            )
                                    .cookie(
                                            csrfData.cookie(),
                                            wrongRefreshToken
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            csrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.status"
                            )
                                    .value(
                                            401
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.code"
                            )
                                    .value(
                                            "AUTH-001"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.message"
                            )
                                    .value(
                                            "유효하지 않은 Refresh Token입니다."
                                    )
                    );
        }

        @Test
        @DisplayName(
                "CSRF 토큰 없이 재발급 요청하면 403 Forbidden을 반환한다"
        )
        void should_ReturnForbidden_when_RefreshRequestHasNoCsrfToken()
                throws Exception {

            createUser(
                    "refreshCsrfUser",
                    "refresh-csrf@test.com",
                    "password"
            );

            LoginData loginData =
                    login(
                            "refreshCsrfUser",
                            "password"
                    );

            mockMvc.perform(
                            post(
                                    "/api/auth/refresh"
                            )
                                    .cookie(
                                            new Cookie(
                                                    REFRESH_TOKEN,
                                                    loginData.refreshToken()
                                            )
                                    )
                    )
                    .andExpect(
                            status().isForbidden()
                    );
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @DisplayName(
                "JWT 인증 사용자가 로그아웃하면 Refresh Token 쿠키를 삭제하고 204를 반환한다"
        )
        void should_DeleteRefreshTokenCookie_when_LogoutSucceeds()
                throws Exception {

            createUser(
                    "logoutUser",
                    "logout-user@test.com",
                    "password"
            );

            LoginData loginData =
                    login(
                            "logoutUser",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            MvcResult result =
                    mockMvc.perform(
                                    post(
                                            "/api/auth/logout"
                                    )
                                            .header(
                                                    AUTHORIZATION,
                                                    BEARER
                                                            + loginData.accessToken()
                                            )
                                            .cookie(
                                                    csrfData.cookie(),
                                                    new Cookie(
                                                            "REFRESH_TOKEN",
                                                            loginData.refreshToken()
                                                    )
                                            )
                                            .header(
                                                    "X-XSRF-TOKEN",
                                                    csrfData.token()
                                            )
                            )
                            .andExpect(
                                    status().isNoContent()
                            )
                            .andExpect(
                                    cookie()
                                            .maxAge(
                                                    "REFRESH_TOKEN",
                                                    0
                                            )
                            )
                            .andReturn();

            Cookie deletedRefreshTokenCookie =
                    result.getResponse()
                            .getCookie(
                                    "REFRESH_TOKEN"
                            );

            assertThat(
                    deletedRefreshTokenCookie
            ).isNotNull();

            assertThat(
                    deletedRefreshTokenCookie.getMaxAge()
            ).isZero();

            assertThat(
                    deletedRefreshTokenCookie.getPath()
            ).isEqualTo("/");

            assertThat(
                    deletedRefreshTokenCookie.isHttpOnly()
            ).isTrue();
        }
    }

    @Nested
    @DisplayName("권한")
    class RoleTest {

        @Test
        @DisplayName(
                "회원가입 시 기본 권한은 USER이다"
        )
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
                            post(
                                    "/api/users"
                            )
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
                            jsonPath(
                                    "$.username"
                            )
                                    .value(
                                            "roleUser"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.role"
                            )
                                    .value(
                                            "USER"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "ADMIN이 사용자 권한을 CHANNEL_MANAGER로 변경하면 200과 변경된 사용자 정보를 반환한다"
        )
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

            LoginData adminLogin =
                    login(
                            "roleAdmin1",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody =
                    """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(
                            userId
                    );

            mockMvc.perform(
                            put(
                                    "/api/auth/role"
                            )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + adminLogin.accessToken()
                                    )
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
                                            requestBody
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.id"
                            )
                                    .value(
                                            userId
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.username"
                            )
                                    .value(
                                            "channelManagerUser"
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.role"
                            )
                                    .value(
                                            "CHANNEL_MANAGER"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "ADMIN이 사용자 권한을 ADMIN으로 변경하면 200과 ADMIN 권한을 반환한다"
        )
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

            LoginData adminLogin =
                    login(
                            "roleAdmin2",
                            "password"
                    );

            CsrfData csrfData =
                    getCsrfData();

            String requestBody =
                    """
                    {
                        "userId": "%s",
                        "newRole": "ADMIN"
                    }
                    """.formatted(
                            userId
                    );

            mockMvc.perform(
                            put(
                                    "/api/auth/role"
                            )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + adminLogin.accessToken()
                                    )
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
                                            requestBody
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.id"
                            )
                                    .value(
                                            userId
                                    )
                    )
                    .andExpect(
                            jsonPath(
                                    "$.role"
                            )
                                    .value(
                                            "ADMIN"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "CSRF 토큰 없이 사용자 권한을 수정하면 403 Forbidden을 반환한다"
        )
        void should_ReturnForbidden_when_RoleUpdateHasNoCsrfToken()
                throws Exception {

            String userId =
                    createUser(
                            "csrfRoleUser",
                            "csrf-role-user@test.com",
                            "password"
                    );

            createAdmin(
                    "csrfRoleAdmin",
                    "csrf-role-admin@test.com",
                    "password"
            );

            LoginData adminLogin =
                    login(
                            "csrfRoleAdmin",
                            "password"
                    );

            String requestBody =
                    """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(
                            userId
                    );

            mockMvc.perform(
                            put(
                                    "/api/auth/role"
                            )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + adminLogin.accessToken()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            requestBody
                                    )
                    )
                    .andExpect(
                            status().isForbidden()
                    );
        }

        @Test
        @DisplayName(
                "로그인 중인 사용자의 Role이 변경되면 기존 Access Token은 무효화된다"
        )
        void should_InvalidateExistingJwt_when_UserRoleIsUpdated()
                throws Exception {

            String targetUserId =
                    createUser(
                            "jwtRoleUser",
                            "jwt-role-user@test.com",
                            "password"
                    );

            LoginData targetLogin =
                    login(
                            "jwtRoleUser",
                            "password"
                    );

            createAdmin(
                    "jwtRoleAdmin",
                    "jwt-role-admin@test.com",
                    "password"
            );

            LoginData adminLogin =
                    login(
                            "jwtRoleAdmin",
                            "password"
                    );

            CsrfData roleCsrfData =
                    getCsrfData();

            String roleRequestBody =
                    """
                    {
                        "userId": "%s",
                        "newRole": "CHANNEL_MANAGER"
                    }
                    """.formatted(
                            targetUserId
                    );

            /*
             * ADMIN이 대상 사용자의 Role을 변경한다.
             */
            mockMvc.perform(
                            put(
                                    "/api/auth/role"
                            )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + adminLogin.accessToken()
                                    )
                                    .cookie(
                                            roleCsrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            roleCsrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            roleRequestBody
                                    )
                    )
                    .andExpect(
                            status().isOk()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.role"
                            )
                                    .value(
                                            "CHANNEL_MANAGER"
                                    )
                    );

            /*
             * Role 변경 시 JwtRegistry에서 대상 사용자의
             * 기존 JWT 정보가 제거된다.
             *
             * 따라서 Role 변경 이전에 발급받았던
             * Access Token은 더 이상 사용할 수 없다.
             */
            mockMvc.perform(
                            get(
                                    "/api/channels"
                            )
                                    .param(
                                            "userId",
                                            targetUserId
                                    )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + targetLogin.accessToken()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    );

            /*
             * 다시 로그인하면 변경된 Role을 기준으로
             * 새로운 JWT가 발급된다.
             */
            LoginData newTargetLogin =
                    login(
                            "jwtRoleUser",
                            "password"
                    );

            CsrfData channelCsrfData =
                    getCsrfData();

            String channelRequestBody =
                    """
                    {
                        "name": "jwt-role-channel",
                        "description": "role updated channel"
                    }
                    """;

            /*
             * 새로운 JWT에는 변경된 CHANNEL_MANAGER 권한이
             * 적용되어 있으므로 채널 생성이 가능하다.
             */
            mockMvc.perform(
                            post(
                                    "/api/channels/public"
                            )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + newTargetLogin.accessToken()
                                    )
                                    .cookie(
                                            channelCsrfData.cookie()
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            channelCsrfData.token()
                                    )
                                    .contentType(
                                            MediaType.APPLICATION_JSON
                                    )
                                    .content(
                                            channelRequestBody
                                    )
                    )
                    .andExpect(
                            status().isCreated()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.name"
                            )
                                    .value(
                                            "jwt-role-channel"
                                    )
                    );
        }

        @Test
        @DisplayName(
                "로그아웃하면 기존 Access Token과 Refresh Token이 모두 무효화된다"
        )
        void should_InvalidateJwtInformation_when_UserLogsOut()
                throws Exception {

            String userId =
                    createUser(
                            "logoutInvalidateUser",
                            "logout-invalidate@test.com",
                            "password"
                    );

            LoginData loginData =
                    login(
                            "logoutInvalidateUser",
                            "password"
                    );

            CsrfData logoutCsrfData =
                    getCsrfData();

            mockMvc.perform(
                            post(
                                    "/api/auth/logout"
                            )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + loginData.accessToken()
                                    )
                                    .cookie(
                                            logoutCsrfData.cookie(),
                                            new Cookie(
                                                    REFRESH_TOKEN,
                                                    loginData.refreshToken()
                                            )
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            logoutCsrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isNoContent()
                    );

            /*
             * 로그아웃 전 Access Token은 Registry에서 제거되었으므로
             * 더 이상 인증에 사용할 수 없다.
             */
            mockMvc.perform(
                            get(
                                    "/api/channels"
                            )
                                    .param(
                                            "userId",
                                            userId
                                    )
                                    .header(
                                            AUTHORIZATION,
                                            BEARER
                                                    + loginData.accessToken()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    );

            /*
             * 기존 Refresh Token 역시 Registry에서 제거되었으므로
             * 재발급할 수 없다.
             */
            CsrfData refreshCsrfData =
                    getCsrfData();

            mockMvc.perform(
                            post(
                                    "/api/auth/refresh"
                            )
                                    .cookie(
                                            refreshCsrfData.cookie(),
                                            new Cookie(
                                                    REFRESH_TOKEN,
                                                    loginData.refreshToken()
                                            )
                                    )
                                    .header(
                                            "X-XSRF-TOKEN",
                                            refreshCsrfData.token()
                                    )
                    )
                    .andExpect(
                            status().isUnauthorized()
                    );
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
                                post(
                                        "/api/users"
                                )
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
                                                objectMapper
                                                        .writeValueAsString(
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

        return body.get(
                        "id"
                )
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
                userRepository
                        .findById(
                                UUID.fromString(
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

    private LoginData login(
            String username,
            String password
    ) throws Exception {

        CsrfData csrfData =
                getCsrfData();

        MvcResult loginResult =
                mockMvc.perform(
                                post(
                                        "/api/auth/login"
                                )
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
                        .andExpect(
                                jsonPath(
                                        "$.accessToken"
                                )
                                        .isNotEmpty()
                        )
                        .andReturn();

        JsonNode body =
                objectMapper.readTree(
                        loginResult.getResponse()
                                .getContentAsString()
                );

        String accessToken =
                body.get(
                                "accessToken"
                        )
                        .asText();

        Cookie refreshTokenCookie =
                loginResult.getResponse()
                        .getCookie(
                                REFRESH_TOKEN
                        );

        assertThat(
                refreshTokenCookie
        ).isNotNull();

        assertThat(
                refreshTokenCookie.getValue()
        ).isNotBlank();

        return new LoginData(
                accessToken,
                refreshTokenCookie.getValue()
        );
    }

    private CsrfData getCsrfData()
            throws Exception {

        MvcResult result =
                mockMvc.perform(
                                get(
                                        "/api/auth/csrf-token"
                                )
                        )
                        .andExpect(
                                status()
                                        .isNonAuthoritativeInformation()
                        )
                        .andReturn();

        Cookie csrfCookie =
                result.getResponse()
                        .getCookie(
                                "XSRF-TOKEN"
                        );

        assertThat(
                csrfCookie
        ).isNotNull();

        assertThat(
                csrfCookie.getValue()
        ).isNotBlank();

        return new CsrfData(
                csrfCookie,
                csrfCookie.getValue()
        );
    }

    private record LoginData(
            String accessToken,
            String refreshToken
    ) {
    }

    private record CsrfData(
            Cookie cookie,
            String token
    ) {
    }
}
