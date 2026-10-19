package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.security.CustomUserDetails;
import com.sprint.mission.discodeit.service.SseEmitterManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

  private final SseEmitterManager sseEmitterManager;

  @GetMapping(
      value = "/api/sse",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE
  )
  public SseEmitter subscribe(
      @AuthenticationPrincipal CustomUserDetails principal,
      @RequestParam(name = "lastEventId", required = false) UUID lastEventId,
      @RequestHeader(name = "Last-Event-ID", required = false) UUID headerLastEventId
  ) {

    UUID effectiveLastEventId =
        lastEventId != null ? lastEventId : headerLastEventId;

    return sseEmitterManager.subscribe(
        principal.getUserDto().id(),
        effectiveLastEventId
    );
  }
}
