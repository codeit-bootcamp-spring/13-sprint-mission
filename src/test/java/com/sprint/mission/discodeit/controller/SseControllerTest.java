package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.service.SseService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(SseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ErrorCodeStatusMapper.class)
@ActiveProfiles("test")
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void connect_인증된_사용자의_SSE_연결을_생성한다()
            throws Exception {
        UUID userId = UUID.randomUUID();

        authenticate(userId);

        SseEmitter emitter = new SseEmitter();

        given(sseService.connect(userId, null))
                .willReturn(emitter);

        mockMvc.perform(
                        get("/api/sse")
                                .accept(
                                        MediaType.TEXT_EVENT_STREAM
                                )
                )
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        then(sseService).should()
                .connect(userId, null);

        emitter.complete();
    }

    @Test
    void connect_LastEventId를_서비스에_전달한다()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();

        authenticate(userId);

        SseEmitter emitter = new SseEmitter();

        given(
                sseService.connect(
                        userId,
                        lastEventId
                )
        ).willReturn(emitter);

        mockMvc.perform(
                        get("/api/sse")
                                .header(
                                        "Last-Event-ID",
                                        lastEventId
                                )
                                .accept(
                                        MediaType.TEXT_EVENT_STREAM
                                )
                )
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted());

        then(sseService).should()
                .connect(userId, lastEventId);

        emitter.complete();
    }

    private void authenticate(UUID userId) {
        DiscodeitUserDetails userDetails =
                userDetails(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private DiscodeitUserDetails userDetails(UUID userId) {
        UserResponse user = new UserResponse(
                userId,
                "codeit",
                "codeit@example.com",
                true,
                null,
                UserRole.USER
        );

        return new DiscodeitUserDetails(
                user,
                "encoded-password"
        );
    }
}