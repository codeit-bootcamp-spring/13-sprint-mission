package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.service.TokenService;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
import com.sprint.mission.discodeit.service.basic.RefreshTokenCookieManagerImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtLoginSuccessHandler.class,
        JwtLogoutHandler.class, RefreshTokenCookieManagerImpl.class})
@EnableConfigurationProperties(RefreshCookieProperties.class)
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("AuthController 슬라이스 테스트")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    JwtRegistry jwtRegistry;

    @MockitoBean
    DiscodeitUserDetailsService userDetailsService;

    @MockitoBean
    LoginFailureHandler loginFailureHandler;

    @MockitoBean
    UserRoleUpdater userRoleUpdater;

    @Test
    @DisplayName("인증 없이 CSRF 정보와 쿠키로 갱신하여 JwtDto를 반환한다")
    void refresh_returnsJwtDtoWithoutAccessToken() throws Exception {
        UserDto user = userDto(UUID.randomUUID(), Role.USER);
        given(tokenService.rotateRefreshToken())
                .willReturn(new JwtDtoWithRefresh(new JwtDto(user, "new-access"), "new-refresh"));

        mockMvc.perform(withCsrf(post("/api/auth/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", "existing-refresh"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDto.id").value(user.id().toString()))
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.userDto.password").doesNotExist());

        // 쿠키의 실제 응답 헤더는 실제 TokenService를 사용하는 HTTP 통합 테스트에서 검증한다.
        then(tokenService).should().addRefreshTokenCookie("new-refresh");
    }

    @Test
    @DisplayName("갱신 실패는 401 오류 JSON으로 응답하고 쿠키를 설정하지 않는다")
    void refresh_returnsUnauthorizedWhenRenewalFails() throws Exception {
        given(tokenService.rotateRefreshToken()).willThrow(new TokenRenewalFailedException());

        mockMvc.perform(withCsrf(post("/api/auth/refresh")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("TOKEN_RENEWAL_FAILED"))
                .andExpect(cookie().doesNotExist("REFRESH_TOKEN"));

        then(tokenService).should(never()).addRefreshTokenCookie(anyString());
    }

    @Test
    @DisplayName("CSRF 정보 없이 갱신하면 서비스 호출 전에 403으로 거부한다")
    void refresh_returnsForbiddenWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("REFRESH_TOKEN", "existing-refresh")))
                .andExpect(status().isForbidden());

        then(tokenService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("CSRF 토큰 발급 요청 시 204 응답과 쿠키를 반환한다")
    void getCsrfToken_returnsCookie() throws Exception {
        String csrfTokenName = "XSRF-TOKEN";

        mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(cookie().exists(csrfTokenName))
                .andExpect(cookie().httpOnly(csrfTokenName, false))
                .andExpect(cookie().path(csrfTokenName, "/"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("사용자 역할 변경 요청 시 변경된 사용자 정보를 반환한다")
    void updateRole_returnsUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(userId, Role.CHANNEL_MANAGER);
        UserDto response = userDto(userId, Role.CHANNEL_MANAGER);
        given(userRoleUpdater.updateRole(userId, request.toCommand())).willReturn(response);

        mockMvc.perform(withCsrf(put("/api/auth/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.role").value(Role.CHANNEL_MANAGER.name()));

        then(userRoleUpdater).should().updateRole(userId, request.toCommand());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("사용자 역할 변경 요청값이 비어 있으면 400을 반환한다")
    void updateRole_returnsBadRequest_whenRequestIsInvalid() throws Exception {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(null, null);

        mockMvc.perform(withCsrf(put("/api/auth/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());

        then(userRoleUpdater).shouldHaveNoInteractions();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("관리자 권한이 없으면 사용자 역할 변경 요청에 403을 반환한다")
    void updateRole_returnsForbidden_whenUserIsNotAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(userId, Role.CHANNEL_MANAGER);

        mockMvc.perform(withCsrf(put("/api/auth/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value(403),
                        jsonPath("$.code").value("AUTH_403")
                );

        then(userRoleUpdater).shouldHaveNoInteractions();
    }

    private UserDto userDto(UUID userId, Role role) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-11T10:00:00+09:00");
        return new UserDto(userId, "testUser", "test@example.com", null, false, role, now, now);
    }

    private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult csrfResult = mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isNoContent())
                .andReturn();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        return request
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue());
    }
}
