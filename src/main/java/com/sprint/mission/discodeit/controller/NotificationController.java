package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.security.CustomUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.SseEmitterManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final SseEmitterManager sseEmitterManager;

  @GetMapping
  public ResponseEntity<List<NotificationDto>> findAll(
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    UUID userId = principal.getUserDto().id();

    log.debug("알림 목록 조회 요청: userId={}", userId);

    List<NotificationDto> response = notificationService.findAllByReceiverId(userId);

    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails principal) {
    UUID userId = principal.getUserDto().id();

    log.debug("SSE 구독 요청: userId={}", userId);

    return sseEmitterManager.subscribe(userId);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    UUID requesterId = principal.getUserDto().id();

    log.debug("알림 삭제 요청: notificationId={}, requesterId={}", notificationId, requesterId);

    notificationService.delete(notificationId, requesterId);

    return ResponseEntity.noContent().build();
  }

}
