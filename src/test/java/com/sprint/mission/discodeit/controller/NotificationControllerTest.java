package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @Mock
  private NotificationService notificationService;

  private MockMvc mockMvc;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    UserDto userDto = new UserDto(
        userId,
        "receiver",
        "receiver@test.com",
        null,
        false,
        Role.USER
    );

    DiscodeitUserDetails principal =
        new DiscodeitUserDetails(userDto, "encodedPassword");
    
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        )
    );

    mockMvc = MockMvcBuilders
        .standaloneSetup(new NotificationController(notificationService))
        .setCustomArgumentResolvers(
            new AuthenticationPrincipalArgumentResolver()
        )
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("알림 조회는 요청 파라미터 대신 로그인한 사용자 ID를 사용한다")
  void getNotifications_UsesAuthenticatedUser() throws Exception {
    UUID notificationId = UUID.randomUUID();

    NotificationDto notification = new NotificationDto(
        notificationId,
        Instant.now(),
        userId,
        "알림 제목",
        "알림 내용"
    );

    given(notificationService.findAllByReceiverId(userId))
        .willReturn(List.of(notification));

    mockMvc.perform(
            get("/api/notifications")
                .param("userId", UUID.randomUUID().toString())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].receiverId").value(userId.toString()));

    then(notificationService).should().findAllByReceiverId(userId);
  }

  @Test
  @DisplayName("알림 삭제 시 로그인한 사용자 ID를 서비스에 전달한다")
  void deleteNotification_UsesAuthenticatedUser() throws Exception {
    UUID notificationId = UUID.randomUUID();

    mockMvc.perform(
            delete("/api/notifications/{notificationId}", notificationId)
        )
        .andExpect(status().isNoContent());

    then(notificationService).should().delete(notificationId, userId);
  }

  @Test
  @DisplayName("다른 사용자의 알림 삭제가 거부되면 HTTP 403을 반환한다")
  void deleteNotification_Forbidden() throws Exception {
    UUID notificationId = UUID.randomUUID();

    willThrow(
        new NotificationAccessDeniedException(
            Map.of("notificationId", notificationId)
        )
    ).given(notificationService).delete(notificationId, userId);

    mockMvc.perform(
            delete("/api/notifications/{notificationId}", notificationId)
        )
        .andExpect(status().isForbidden());
  }
}