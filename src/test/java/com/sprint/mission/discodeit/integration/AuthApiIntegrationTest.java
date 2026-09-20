package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("CSRF 토큰 발급 API 통합 테스트")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getCsrfToken_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isNonAuthoritativeInformation())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    @DisplayName("로그인 API 통합 테스트 - 성공")
    void login_Success() throws Exception {
        // Given
        UserCreateRequest userRequest = new UserCreateRequest(
                "loginuser",
                "login@example.com",
                "Password1!"
        );
        userService.create(userRequest, Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "loginuser")
                        .param("password", "Password1!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("loginuser")))
                .andExpect(jsonPath("$.email", is("login@example.com")));
    }

    @Test
    @DisplayName("로그인 API 통합 테스트 - 실패 (존재하지 않는 사용자)")
    void login_Failure_UserNotFound() throws Exception {
        // When & Then
        // 사용자 존재 여부를 노출하지 않기 위해 404가 아닌 401로 응답한다
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "nonexistentuser")
                        .param("password", "Password1!")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("로그인 API 통합 테스트 - 실패 (잘못된 비밀번호)")
    void login_Failure_InvalidCredentials() throws Exception {
        // Given
        UserCreateRequest userRequest = new UserCreateRequest(
                "loginuser2",
                "login2@example.com",
                "Password1!"
        );
        userService.create(userRequest, Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "loginuser2")
                        .param("password", "WrongPassword1!")
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("로그인 API 통합 테스트 - 실패 (자격 증명 누락)")
    void login_Failure_EmptyCredentials() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "")
                        .param("password", "")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 API 통합 테스트")
    void logout_Success() throws Exception {
        // Given
        UserCreateRequest userRequest = new UserCreateRequest(
                "logoutuser",
                "logout@example.com",
                "Password1!"
        );
        userService.create(userRequest, Optional.empty());

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "logoutuser")
                        .param("password", "Password1!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 세션이 무효화되었는지 확인
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("사용자 권한 수정 API 통합 테스트 - 성공")
    @WithMockUser(roles = "ADMIN")
    void updateRole_Success() throws Exception {
        // Given
        UserDto user = userService.create(
            new UserCreateRequest("roleuser", "role@example.com", "Password1!"),
            Optional.empty()
        );
        RoleUpdateRequest request = new RoleUpdateRequest(user.id(), Role.CHANNEL_MANAGER);

        // When & Then
        mockMvc.perform(put("/api/auth/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(user.id().toString())))
            .andExpect(jsonPath("$.role", is("CHANNEL_MANAGER")));
    }

    @Test
    @DisplayName("사용자 권한 수정 API 통합 테스트 - 권한 부족")
    @WithMockUser(roles = "USER")
    void updateRole_Forbidden() throws Exception {
        // Given
        RoleUpdateRequest request = new RoleUpdateRequest(UUID.randomUUID(), Role.ADMIN);

        // When & Then
        mockMvc.perform(put("/api/auth/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isForbidden());
    }
}
