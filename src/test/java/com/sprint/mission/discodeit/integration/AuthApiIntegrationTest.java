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
import jakarta.servlet.http.Cookie;
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
    @DisplayName("현재 사용자 조회 API 통합 테스트 - 로그인 세션으로 조회")
    void getCurrentUser_Success() throws Exception {
        // Given
        userService.create(
            new UserCreateRequest("meuser", "me@example.com", "Password1!"),
            Optional.empty()
        );

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "meuser")
                .param("password", "Password1!")
                .with(csrf()))
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.username", is("meuser")))
            .andExpect(jsonPath("$.email", is("me@example.com")));
    }

    @Test
    @DisplayName("현재 사용자 조회 API 통합 테스트 - 비인증 요청")
    void getCurrentUser_Unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
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

    @Test
    @DisplayName("RememberMe 통합 테스트 - 세션 없이 remember-me 쿠키만으로 인증 유지")
    void rememberMe_Success() throws Exception {
        // Given
        userService.create(
            new UserCreateRequest("rememberuser", "remember@example.com", "Password1!"),
            Optional.empty()
        );

        // 로그인 유지 체크(remember-me=true)로 로그인
        Cookie rememberMeCookie = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "rememberuser")
                .param("password", "Password1!")
                .param("remember-me", "true")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("remember-me"))
            .andReturn()
            .getResponse()
            .getCookie("remember-me");

        // When & Then
        // JSESSIONID 없이 remember-me 쿠키만 보내도 인증이 유지된다
        mockMvc.perform(get("/api/auth/me")
                .cookie(rememberMeCookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("rememberuser")))
            .andExpect(jsonPath("$.email", is("remember@example.com")));
    }

    @Test
    @DisplayName("RememberMe 통합 테스트 - remember-me 파라미터가 없으면 쿠키가 발급되지 않는다")
    void rememberMe_NotRequested() throws Exception {
        // Given
        userService.create(
            new UserCreateRequest("norememberuser", "noremember@example.com", "Password1!"),
            Optional.empty()
        );

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "norememberuser")
                .param("password", "Password1!")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(cookie().doesNotExist("remember-me"));
    }
}
