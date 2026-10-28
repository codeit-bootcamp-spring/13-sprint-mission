package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.sse.SseService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {
  private final SseService sseService;

  @GetMapping(value = "/api/sse", produces = "text/event-stream")
  public SseEmitter connect(@AuthenticationPrincipal DiscodeitUserDetails user,
      @RequestHeader(value = "Last-Event-ID", required = false) UUID lastEventId,
      @RequestHeader(value = "LastEventId", required = false) UUID legacyLastEventId,
      @RequestParam(value = "lastEventId", required = false) UUID queryLastEventId) {
    return sseService.connect(user.getUserDto().id(),
        lastEventId != null ? lastEventId
            : legacyLastEventId != null ? legacyLastEventId : queryLastEventId);
  }
}
