package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Nested
    @DisplayName("CSRF 토큰")
    class CsrfTokenTest {

        @Test
        @DisplayName("CSRF 토큰 요청 시 203과 XSRF-TOKEN 쿠키를 반환한다")
        void should_ReturnCsrfTokenCookie_when_CsrfTokenIsRequested()
                throws Exception {

            // when
            MvcResult result = mockMvc.perform(
                            get("/api/auth/csrf-token")
                    )
                    .andExpect(status().isNonAuthoritativeInformation())
                    .andReturn();

            // then
            Cookie csrfCookie =
                    result.getResponse().getCookie("XSRF-TOKEN");

            assertThat(csrfCookie).isNotNull();
            assertThat(csrfCookie.getValue()).isNotBlank();
            assertThat(csrfCookie.isHttpOnly()).isFalse();
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName("올바른 사용자 이름과 비밀번호로 로그인하면 200과 사용자 정보를 반환한다")
        void should_ReturnUserResponse_when_CredentialsAreValid()
                throws Exception {

            // given
            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            CsrfData csrfData = getCsrfData();

            // when & then
            mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(csrfData.cookie())
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
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.username")
                                    .value("securityUser")
                    )
                    .andExpect(
                            jsonPath("$.email")
                                    .value("security-user@test.com")
                    )
                    .andExpect(
                            jsonPath("$.id")
                                    .isNotEmpty()
                    );
        }

        @Test
        @DisplayName("비밀번호가 틀리면 401과 ErrorResponse를 반환한다")
        void should_ReturnUnauthorized_when_PasswordIsIncorrect()
                throws Exception {

            // given
            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            CsrfData csrfData = getCsrfData();

            // when & then
            mockMvc.perform(
                            post("/api/auth/login")
                                    .cookie(csrfData.cookie())
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
                    .andExpect(status().isUnauthorized())
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
                                    .value("BadCredentialsException")
                    );
        }

        @Test
        @DisplayName("CSRF 토큰 없이 로그인하면 403 Forbidden을 반환한다")
        void should_ReturnForbidden_when_CsrfTokenIsMissing()
                throws Exception {

            // given
            createUser(
                    "securityUser",
                    "security-user@test.com",
                    "password"
            );

            // when & then
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
                    .andExpect(status().isForbidden());
        }
    }

    /*
     * 테스트용 사용자를 실제 회원가입 API로 생성한다.
     *
     * 기존 CRUD 통합 테스트와 달리 Security 테스트는
     * SecurityFilterChain을 실제로 활성화해야 하므로,
     * 회원가입 POST 요청에도 CSRF 토큰을 함께 전송한다.
     */
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

        CsrfData csrfData = getCsrfData();

        MvcResult result = mockMvc.perform(
                        post("/api/users")
                                .cookie(csrfData.cookie())
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
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return body.get("id").asText();
    }

    /*
     * /api/auth/csrf-token을 호출해
     * XSRF-TOKEN 쿠키와 토큰 값을 얻는다.
     */
    private CsrfData getCsrfData()
            throws Exception {

        MvcResult result = mockMvc.perform(
                        get("/api/auth/csrf-token")
                )
                .andExpect(
                        status().isNonAuthoritativeInformation()
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