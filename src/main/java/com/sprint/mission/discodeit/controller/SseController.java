package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.security.CustomUserDetails;
import com.sprint.mission.discodeit.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

  private final SseEmitterManager sseEmitterManager;

  @GetMapping(value = "/api/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails principal) {
    return sseEmitterManager.subscribe(principal.getUserDto().id());
  }
}
