package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.config.SecurityConfig;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SseController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtRegistry jwtRegistry;

    @MockitoBean
    private JwtLoginSuccessHandler jwtLoginSuccessHandler;

    @MockitoBean
    private LoginFailureHandler loginFailureHandler;

    @MockitoBean
    private JwtLogoutHandler jwtLogoutHandler;

    @Test
    @DisplayName("인증된 사용자는 SSE 연결을 요청할 수 있다")
    void connect_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto userDto = mock(UserDto.class);

        given(userDto.id()).willReturn(userId);
        given(userDto.role()).willReturn(Role.USER);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        SseEmitter emitter = new SseEmitter();

        given(sseService.connect(eq(userId), isNull()))
                .willReturn(emitter);

        // when & then
        mockMvc.perform(
                get("/api/sse")
                        .with(user(userDetails))
                        .accept(MediaType.TEXT_EVENT_STREAM)
        ).andExpect(status().isOk());

        then(sseService).should()
                .connect(userId, null);
    }

    @Test
    @DisplayName("SSE 재연결 시 Last-Event-ID를 Service에 전달한다")
    void connect_withLastEventId() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();

        UserDto userDto = mock(UserDto.class);
        given(userDto.id()).willReturn(userId);
        given(userDto.role()).willReturn(Role.USER);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        SseEmitter emitter = new SseEmitter();

        given(sseService.connect(userId, lastEventId))
                .willReturn(emitter);

        // when & then
        mockMvc.perform(
                        get("/api/sse")
                                .with(user(userDetails))
                                .header("Last-Event-ID", lastEventId.toString())
                                .accept(MediaType.TEXT_EVENT_STREAM)
                )
                .andExpect(status().isOk());

        then(sseService).should()
                .connect(userId, lastEventId);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 SSE 연결을 요청할 수 없다")
    void connect_unauthorized() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/api/sse")
                                .accept(MediaType.TEXT_EVENT_STREAM)
                )
                .andExpect(status().isUnauthorized());

        then(sseService).shouldHaveNoInteractions();
    }

}
