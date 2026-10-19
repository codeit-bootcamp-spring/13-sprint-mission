package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.exception.ErrorCodeStatusMapper;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ErrorCodeStatusMapper.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findAll_로그인한_사용자의_알림을_반환한다()
            throws Exception {
        UUID userId = UUID.randomUUID();

        authenticate(userId);

        List<NotificationDto> notifications = List.of(
                new NotificationDto(
                        UUID.randomUUID(),
                        Instant.now(),
                        userId,
                        "첫 번째 알림",
                        "첫 번째 내용"
                ),
                new NotificationDto(
                        UUID.randomUUID(),
                        Instant.now(),
                        userId,
                        "두 번째 알림",
                        "두 번째 내용"
                )
        );

        given(
                notificationService.findAllByReceiverId(userId)
        ).willReturn(notifications);

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(
                        jsonPath("$[0].receiverId")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$[0].title")
                                .value("첫 번째 알림")
                )
                .andExpect(
                        jsonPath("$[1].title")
                                .value("두 번째 알림")
                );

        then(notificationService)
                .should()
                .findAllByReceiverId(userId);
    }

    @Test
    void delete_로그인한_사용자의_알림을_삭제한다()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        authenticate(userId);

        mockMvc.perform(
                        delete(
                                "/api/notifications/{notificationId}",
                                notificationId
                        )
                )
                .andExpect(status().isNoContent());

        then(notificationService)
                .should()
                .delete(notificationId, userId);
    }

    @Test
    void delete_다른_사용자의_알림이면_403을_반환한다()
            throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        authenticate(userId);

        willThrow(
                new NotificationAccessDeniedException(
                        notificationId,
                        userId
                )
        ).given(notificationService)
                .delete(notificationId, userId);

        mockMvc.perform(
                        delete(
                                "/api/notifications/{notificationId}",
                                notificationId
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "NOTIFICATION_ACCESS_DENIED"
                                )
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "본인의 알림만 확인할 수 있습니다."
                                )
                )
                .andExpect(
                        jsonPath("$.details.notificationId")
                                .value(notificationId.toString())
                )
                .andExpect(
                        jsonPath("$.details.requesterId")
                                .value(userId.toString())
                );

        then(notificationService)
                .should()
                .delete(notificationId, userId);
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