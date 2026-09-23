package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DiscodeitUserDetailsService userDetailsService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getCsrfToken_CSRF_토큰을_쿠키에_저장하고_203을_반환한다()
            throws Exception {
        mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isNonAuthoritativeInformation())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void login_올바른_자격_증명이면_사용자_정보를_반환한다()
            throws Exception {
        UUID userId = UUID.randomUUID();

        UserResponse userResponse = createUserResponse(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(
                        userResponse,
                        new BCryptPasswordEncoder().encode("password123")
                );

        given(userDetailsService.loadUserByUsername("codeit"))
                .willReturn(userDetails);

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )
                                .param("username", "codeit")
                                .param("password", "password123")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("codeit"))
                .andExpect(
                        jsonPath("$.email")
                                .value("codeit@example.com")
                );
    }

    @Test
    void login_잘못된_자격_증명이면_401을_반환한다()
            throws Exception {
        given(userDetailsService.loadUserByUsername("unknown"))
                .willThrow(
                        new UsernameNotFoundException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )
                                .param("username", "unknown")
                                .param("password", "wrong-password")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_CREDENTIALS")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "아이디 또는 비밀번호가 일치하지 않습니다."
                                )
                );
    }

    @Test
    void getCurrentUser_로그인한_사용자_정보를_반환한다()
            throws Exception {
        UUID userId = UUID.randomUUID();

        UserResponse userResponse = createUserResponse(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(
                        userResponse,
                        "encoded-password"
                );

        mockMvc.perform(
                        get("/api/auth/me")
                                .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("codeit"))
                .andExpect(
                        jsonPath("$.email")
                                .value("codeit@example.com")
                );
    }

    @Test
    void getCurrentUser_로그인하지_않으면_401을_반환한다()
            throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_로그인한_사용자의_세션을_종료하고_204를_반환한다()
            throws Exception {
        UUID userId = UUID.randomUUID();

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(
                        createUserResponse(userId),
                        "encoded-password"
                );

        mockMvc.perform(
                        post("/api/auth/logout")
                                .with(csrf())
                                .with(user(userDetails))
                )
                .andExpect(status().isNoContent());
    }

    private UserResponse createUserResponse(UUID userId) {
        return new UserResponse(
                userId,
                "codeit",
                "codeit@example.com",
                false,
                null
        );
    }
}