package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.RememberMeProperties;
import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.security.handler.LoginSuccessHandler;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@EnableConfigurationProperties(RememberMeProperties.class)
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("AuthController 슬라이스 테스트")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    LoginSuccessHandler loginSuccessHandler;

    @MockitoBean
    LoginFailureHandler loginFailureHandler;

    @MockitoBean
    UserRoleUpdater userRoleUpdater;

    @Test
    @DisplayName("CSRF 토큰 발급 요청 시 203 응답과 쿠키를 반환한다")
    void getCsrfToken_returnsCookie() throws Exception {
        String csrfTokenName = "XSRF-TOKEN";

        mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isNonAuthoritativeInformation())
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
                .andExpect(status().isNonAuthoritativeInformation())
                .andReturn();
        Cookie csrfCookie = csrfResult.getResponse().getCookie("XSRF-TOKEN");

        return request
                .cookie(csrfCookie)
                .header("X-XSRF-TOKEN", csrfCookie.getValue());
    }
}
