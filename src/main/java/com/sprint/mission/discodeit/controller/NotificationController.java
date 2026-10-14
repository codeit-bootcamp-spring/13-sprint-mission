package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService service;

  @GetMapping
  public List<NotificationDto> findAll(@AuthenticationPrincipal DiscodeitUserDetails principal) {
    return service.findAll(principal.getUserDto().id());
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> read(@PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails principal) {
    service.read(notificationId, principal.getUserDto().id());
    return ResponseEntity.noContent().build();
  }
}
