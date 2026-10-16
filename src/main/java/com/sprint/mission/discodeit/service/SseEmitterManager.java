package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterManager {

  private final Map<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(UUID userId) {
    SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

    emitters.compute(userId, (id, current) -> {
      if (current == null) {
        current = ConcurrentHashMap.newKeySet();
      }
      current.add(emitter);
      return current;
    });

    Runnable cleanup = () ->
        emitters.computeIfPresent(userId, (id, current) -> {
          current.remove(emitter);
          return current.isEmpty() ? null : current;
        });

    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(error -> cleanup.run());

    try {
      emitter.send(
          SseEmitter.event()
              .name("connected")
              .data("SSE 연결 성공")
      );
    } catch (IOException e) {
      cleanup.run();
      throw new IllegalStateException("SSE 연결 초기화 실패", e);
    }

    return emitter;
  }

  public void send(UUID userId, NotificationDto notification) {
    Set<SseEmitter> userEmitters = emitters.get(userId);

    if (userEmitters == null) {
      return;
    }

    for (SseEmitter emitter : userEmitters) {
      try {
        emitter.send(
            SseEmitter.event()
                .id(notification.id().toString())
                .name("notification")
                .data(notification)
        );
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
    }
  }
}
